package com.dst.rpc.socket

import com.dst.rpc.RPCAddress
import com.dst.rpc.socket.serializer.SerializeReader
import com.dst.rpc.socket.serializer.SerializeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

internal fun RPCorrelator(sourceAddress: RPCAddress, remoteAddress: RPCAddress, connectionTimeout: Long): RPCorrelator =
    RPCorrelatorImpl(sourceAddress, remoteAddress, connectionTimeout)

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
        val serializeWriter = SerializeWriter()
        serializeWriter.writeString(functionOwner.name)
        serializeWriter.writeString(functionName)
        serializeWriter.writeList(argumentTypes.map { it.name })
        serializeWriter.writeList(argumentValue)

        socket.getOutputStream().write(serializeWriter.toByteArray())
        socket.getOutputStream().flush()
        socket.getOutputStream().close()

        val serializeReader = SerializeReader(socket.getInputStream())
        val data = serializeReader.readValue<Any?>()
        val throwable = serializeReader.readValue<Throwable?>()
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
        val socketDeferred = async {
            this@RPCorrelatorImpl.connect()
        }
        val byteArrayDeferred = async {
            val serializeWriter = SerializeWriter()
            serializeWriter.writeString(functionOwner.name)
            serializeWriter.writeString(functionName)
            serializeWriter.writeList(argumentTypes.map { it.name })
            serializeWriter.writeList(argumentValue)
            serializeWriter.writeSerializable(this@RPCorrelatorImpl.sourceAddress)
            serializeWriter.close()
            serializeWriter.toByteArray()
        }
        val socket = socketDeferred.await()
        val byteArray = byteArrayDeferred.await()
        suspendCoroutineUninterceptedOrReturn { continuation ->
            val callback = SocketRPCallback { data, throwable ->
                if (throwable != null) {
                    continuation.resumeWithException(throwable)
                } else continuation.resume(data)
            }
            socket.getOutputStream().write(byteArray)
            socket.getOutputStream().flush()
            socket.getOutputStream().close()

            val serializeReader = SerializeReader(socket.getInputStream())
            val data = serializeReader.readValue<Any?>()
            val throwable = serializeReader.readValue<Throwable?>()
            if (throwable != null) {
                throw throwable
            }
            data
        }
    }

    private fun connect(): Socket {
        val socket = Socket()
        val host = "${this.remoteAddress.scheme}://${this.remoteAddress.domain}"
        val socketAddress = InetSocketAddress(host, this.remoteAddress.port)
        socket.connect(socketAddress, this.connectionTimeout.toInt())
        return socket
    }
}