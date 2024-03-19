package com.dst.rpc.socket

import com.dst.rpc.OneShotContinuation
import com.dst.rpc.Address
import com.dst.rpc.CallService
import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard
import com.dst.rpc.socket.serializer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal fun CallService(
    sourceAddress: Address,
    remoteAddress: Address,
    connectionTimeout: Long
): CallService = SocketCallServiceProxy(sourceAddress, remoteAddress, connectionTimeout)

internal fun CallService() : CallService = SocketCallServiceStub()

private class SocketCallServiceProxy(
    private val sourceAddress: Address,
    private val remoteAddress: Address,
    private val connectionTimeout: Long
) : CallService {

    private var _socket: Socket? = null

    override val isOpen: Boolean get() = this._socket.let { socket -> socket != null && !socket.isClosed }

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val socket = this.connect()
        val byteArray = SerializeWriter().also { serializeWriter ->
            serializeWriter.writeString(KEY_FUNCTION_TYPE_NON_SUSPEND)
            serializeWriter.writeString(functionOwner.name)
            serializeWriter.writeString(functionName)
            serializeWriter.writeString(functionUniqueKey)
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
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = coroutineScope {
        val socket = withContext(Dispatchers.IO) {
            this@SocketCallServiceProxy.connect()
        }
        suspendCoroutineUninterceptedOrReturn { continuation ->
            var token: Long = 0L
            val oneShotContinuation = OneShotContinuation(continuation, this.coroutineContext)
            val timeoutWaitingTask = launch {
                delay(this@SocketCallServiceProxy.connectionTimeout)
                oneShotContinuation.resumeWithException(Throwable("connection timeout for function call: ${functionOwner.name}#${functionName}"))
            }
            val callback = SocketCallback { asyncData, asyncThrowable ->
                timeoutWaitingTask.cancel()
                SocketAsyncCallbackRegistry.removeCallback(token)
                if (asyncThrowable != null) {
                    oneShotContinuation.resumeWithException(asyncThrowable)
                } else oneShotContinuation.resume(asyncData)
            }
            token = SocketAsyncCallbackRegistry.addCallback(callback)
            val byteArray = SerializeWriter().also { serializeWriter ->
                serializeWriter.writeString(KEY_FUNCTION_TYPE_SUSPEND)
                serializeWriter.writeString(functionOwner.name)
                serializeWriter.writeString(functionName)
                serializeWriter.writeString(functionUniqueKey)
                serializeWriter.writeList(argumentTypes.map { it.name })
                serializeWriter.writeList(argumentValue)
                serializeWriter.writeSerializable(this@SocketCallServiceProxy.sourceAddress)
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
                SocketAsyncCallbackRegistry.removeCallback(token)
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

private class SocketCallServiceStub : CallService {

    override val isOpen: Boolean get() = true

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        return ClientManager.getService(functionOwner as Class<INoProguard>, functionUniqueKey.isNotEmpty())
            .invokeNonSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue)
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        return ClientManager.getService(functionOwner as Class<INoProguard>, functionUniqueKey.isNotEmpty())
            .invokeSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes + Continuation::class.java, argumentValue)
    }
}