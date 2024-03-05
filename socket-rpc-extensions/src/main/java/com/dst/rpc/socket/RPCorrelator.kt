package com.dst.rpc.socket

import com.dst.rpc.RPCAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

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

    override val isOpen: Boolean
        get() = TODO("Not yet implemented")

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val socket = this.connect()
        return super.callFunction(functionOwner, functionName, argumentTypes, argumentValue)
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val socket = withContext(Dispatchers.IO) {
            this@RPCorrelatorImpl.connect()
        }
        return super.callSuspendFunction(functionOwner, functionName, argumentTypes, argumentValue)
    }

    private fun connect(): Socket {
        val socket = Socket()
        val host = "${this.remoteAddress.scheme}://${this.remoteAddress.domain}"
        val socketAddress = InetSocketAddress(host, this.remoteAddress.port)
        socket.connect(socketAddress, this.connectionTimeout.toInt())
        return socket
    }
}