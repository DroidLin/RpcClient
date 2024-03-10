package com.dst.rpc.socket

import java.util.concurrent.atomic.AtomicLong

/**
 * @author liuzhongao
 * @since 2024/3/7 15:59
 */
internal object SocketAsyncInvocationRegistry {

    private val atomicLong = AtomicLong(Long.MIN_VALUE)
    private val rpCallbackMap: MutableMap<Long, SocketCallback> = HashMap()

    fun addRPCallback(rpCallback: SocketCallback): Long {
        return synchronized(this.rpCallbackMap) {
            val token = atomicLong.incrementAndGet()
            this.rpCallbackMap[token] = rpCallback
            token
        }
    }

    fun getRPCallback(token: Long): SocketCallback? {
        return synchronized(this.rpCallbackMap) {
            this.rpCallbackMap[token]
        }
    }

    fun removeRPCallback(token: Long) {
        synchronized(this.rpCallbackMap) {
            this.rpCallbackMap.remove(token)
        }
    }
}