package com.dst.rpc.socket

import com.android.dependencies.serializer.SerializeWriter
import com.dst.rpc.Address
import com.dst.rpc.CallService
import java.net.InetSocketAddress
import java.net.Socket

/**
 * @author liuzhongao
 * @since 2024/3/7 00:32
 */
internal fun interface SocketCallback : CallService.Callback {

    override fun callback(data: Any?, throwable: Throwable?)
}

internal fun SocketCallback(sourceAddress: Address, callbackToken: Long): SocketCallback =
    SocketCallbackImpl(sourceAddress, callbackToken)

private class SocketCallbackImpl(
    private val sourceAddress: Address,
    private val callbackToken: Long
) : SocketCallback {
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
        tempSocket.shutdownInput()
        tempSocket.close()
    }
}