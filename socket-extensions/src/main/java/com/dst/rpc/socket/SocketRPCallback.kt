package com.dst.rpc.socket

import com.dst.rpc.RPCAddress
import com.dst.rpc.socket.serializer.KEY_FUNCTION_SUSPEND_CALLBACK
import com.dst.rpc.socket.serializer.SerializeWriter
import java.net.InetSocketAddress
import java.net.Socket

/**
 * @author liuzhongao
 * @since 2024/3/7 00:32
 */
internal fun interface SocketRPCallback {

    fun callback(data: Any?, throwable: Throwable?)
}

internal fun SocketRPCallback(sourceAddress: RPCAddress, callbackToken: Long): SocketRPCallback =
    SocketRPCallbackImpl(sourceAddress, callbackToken)

private class SocketRPCallbackImpl(
    private val sourceAddress: RPCAddress,
    private val callbackToken: Long
) : SocketRPCallback {
    override fun callback(data: Any?, throwable: Throwable?) {
        val tempSocket = Socket().also { socket ->
            val callbackAddress = InetSocketAddress(this.sourceAddress.domain, this.sourceAddress.port)
            socket.connect(callbackAddress)
        }
        val byteArray = SerializeWriter().also { serializeWriter ->
            serializeWriter.writeString(KEY_FUNCTION_SUSPEND_CALLBACK)
            serializeWriter.writeLong(this.callbackToken)
            serializeWriter.writeValue(data)
            serializeWriter.writeValue(throwable)
            serializeWriter.close()
        }.toByteArray()
        tempSocket.getOutputStream().write(byteArray)
        tempSocket.getOutputStream().flush()
        tempSocket.getOutputStream().close()
        tempSocket.shutdownOutput()
    }
}