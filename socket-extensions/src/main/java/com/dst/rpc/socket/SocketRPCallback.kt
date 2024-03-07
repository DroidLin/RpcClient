package com.dst.rpc.socket

import com.dst.rpc.RPCAddress

/**
 * @author liuzhongao
 * @since 2024/3/7 00:32
 */
internal fun interface SocketRPCallback {

    fun callback(data: Any?, throwable: Throwable?)
}

private class SocketRPCallbackImpl(private val sourceAddress: RPCAddress): SocketRPCallback {
    override fun callback(data: Any?, throwable: Throwable?) {
        TODO("Not yet implemented")
    }
}