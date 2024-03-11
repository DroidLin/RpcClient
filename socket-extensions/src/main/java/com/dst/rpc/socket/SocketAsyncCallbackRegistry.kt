package com.dst.rpc.socket

import java.util.concurrent.atomic.AtomicLong

/**
 * @author liuzhongao
 * @since 2024/3/7 15:59
 */
internal object SocketAsyncCallbackRegistry {

    private val atomicLong = AtomicLong(Long.MIN_VALUE)
    private val innerCallbackMap: MutableMap<Long, SocketCallback> = HashMap()

    fun addCallback(callback: SocketCallback): Long {
        return synchronized(this.innerCallbackMap) {
            var token: Long = 0L
            do {
                token = atomicLong.incrementAndGet()
            } while (this.innerCallbackMap.contains(token))
            this.innerCallbackMap[token] = callback
            token
        }
    }

    fun getCallback(token: Long): SocketCallback? {
        return synchronized(this.innerCallbackMap) {
            this.innerCallbackMap[token]
        }
    }

    fun removeCallback(token: Long) {
        synchronized(this.innerCallbackMap) {
            if (this.innerCallbackMap.contains(token)) {
                this.innerCallbackMap.remove(token)
            }
        }
    }
}