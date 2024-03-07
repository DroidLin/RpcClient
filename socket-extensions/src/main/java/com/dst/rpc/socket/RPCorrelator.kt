package com.dst.rpc.socket

import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard
import com.dst.rpc.OneShotContinuation
import com.dst.rpc.RPCAddress
import com.dst.rpc.socket.serializer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author liuzhongao
 * @since 2024/3/5 23:39
 */
internal interface RPCorrelator {

    val isOpen: Boolean

    fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = null

    suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = null
}

internal fun RPCorrelator(
    sourceAddress: RPCAddress,
    remoteAddress: RPCAddress,
    connectionTimeout: Long
): RPCorrelator = RPCorrelatorImpl(sourceAddress, remoteAddress, connectionTimeout)

internal fun RPCorrelator() : RPCorrelator = RPCorrelatorStub()

private class RPCorrelatorImpl(
    private val sourceAddress: RPCAddress,
    private val remoteAddress: RPCAddress,
    private val connectionTimeout: Long
) : RPCorrelator {

    private var _socket: Socket? = null

    override val isOpen: Boolean get() = this._socket.let { socket -> socket != null && !socket.isClosed }

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val socket = this.connect()
        val byteArray = SerializeWriter().also { serializeWriter ->
            serializeWriter.writeString(KEY_FUNCTION_TYPE_NON_SUSPEND)
            serializeWriter.writeString(functionOwner.name)
            serializeWriter.writeString(functionName)
            serializeWriter.writeList(argumentTypes.map { it.name })
            serializeWriter.writeList(argumentValue)
        }.toByteArray()

        socket.getOutputStream().write(byteArray)
        socket.getOutputStream().flush()
        socket.shutdownOutput()

        val serializeReader = SerializeReader(socket.getInputStream())
        val data = serializeReader.readValue<Any?>()
        val throwable = serializeReader.readValue<Throwable?>()
        socket.close()
        if (throwable != null) {
            throw throwable
        }

        return data
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = coroutineScope {
        val socket = withContext(Dispatchers.IO) {
            this@RPCorrelatorImpl.connect()
        }
        suspendCoroutineUninterceptedOrReturn { continuation ->
            var token: Long = 0L
            val oneShotContinuation = OneShotContinuation(continuation, this.coroutineContext)
            val timeoutWaitingTask = launch {
                delay(this@RPCorrelatorImpl.connectionTimeout)
                oneShotContinuation.resumeWithException(Throwable("connection timeout for function call: ${functionOwner.name}#${functionName}"))
            }
            val callback = SocketRPCallback { asyncData, asyncThrowable ->
                timeoutWaitingTask.cancel()
                SocketAsyncInvocationRegistry.removeRPCallback(token)
                if (asyncThrowable != null) {
                    oneShotContinuation.resumeWithException(asyncThrowable)
                } else oneShotContinuation.resume(asyncData)
            }
            token = SocketAsyncInvocationRegistry.addRPCallback(callback)
            val byteArray = SerializeWriter().also { serializeWriter ->
                serializeWriter.writeString(KEY_FUNCTION_TYPE_SUSPEND)
                serializeWriter.writeString(functionOwner.name)
                serializeWriter.writeString(functionName)
                serializeWriter.writeList(argumentTypes.map { it.name })
                serializeWriter.writeList(argumentValue)
                serializeWriter.writeSerializable(this@RPCorrelatorImpl.sourceAddress)
                serializeWriter.writeLong(token)
                serializeWriter.close()
            }.toByteArray()
            socket.getOutputStream().write(byteArray)
            socket.getOutputStream().flush()
            socket.shutdownOutput()

            val serializeReader = SerializeReader(socket.getInputStream())
            val data = serializeReader.readValue<Any?>()
            val throwable = serializeReader.readValue<Throwable?>()
            socket.close()
            timeoutWaitingTask.cancel()
            if (throwable != null) {
                throw throwable
            }
            if (data != COROUTINE_SUSPENDED) {
                SocketAsyncInvocationRegistry.removeRPCallback(token)
            }
            data
        }
    }

    private fun connect(): Socket {
        val socket = Socket()
        val socketAddress = InetSocketAddress(this.remoteAddress.domain, this.remoteAddress.port)
        socket.connect(socketAddress, this.connectionTimeout.toInt())
        return socket
    }
}

private class RPCorrelatorStub : RPCorrelator {

    override val isOpen: Boolean get() = true

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val functionOwnerImplementation = ClientManager.getService(functionOwner as Class<INoProguard>)
        return functionOwner.getDeclaredMethod(functionName, *argumentTypes.toTypedArray())
            .invoke(functionOwnerImplementation, *argumentValue.toTypedArray())
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val functionOwnerImplementation = ClientManager.getService(functionOwner as Class<INoProguard>)
        return functionOwner.getDeclaredMethod(functionName, *argumentTypes.toTypedArray())
            .invokeSuspend(functionOwnerImplementation, *argumentValue.toTypedArray())
    }
}