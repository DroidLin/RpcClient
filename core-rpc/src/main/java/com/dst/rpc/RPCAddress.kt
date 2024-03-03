package com.dst.rpc

import java.io.Serial
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/3 14:46
 */
interface RPCAddress : Serializable {
    val scheme: String
    val domain: String
    val port: Int

    val value: String get() = ""

    companion object : RPCAddress {
        private const val serialVersionUID: Long = 5562944500009031836L
        override val scheme: String get() = ""
        override val domain: String get() = ""
        override val port: Int get() = -1
    }
}