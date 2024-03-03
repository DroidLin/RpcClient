package com.dst.rpc

import java.io.Serial
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/1 00:52
 */
interface Response : Serializable {

    val data: Any?

    /**
     * exception will be caught and be transported to client.
     */
    val throwable: Throwable?

    companion object : Response {
        private const val serialVersionUID: Long = -8536954738269482424L
        override val data: Any? get() = null
        override val throwable: Throwable? get() = null
    }
}