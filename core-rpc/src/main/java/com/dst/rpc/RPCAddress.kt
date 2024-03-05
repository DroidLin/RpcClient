package com.dst.rpc

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

@JvmOverloads
fun RPCAddress(scheme: String = "", domain: String = "", port: Int = -1, value: String? = null): RPCAddress {
    val rawValue = value ?: if (port > 0) {
        "${scheme}://${domain}:${port}"
    } else "${scheme}://${domain}"
    return RPCAddressImpl(scheme, domain, port, rawValue)
}

private class RPCAddressImpl(
    override val scheme: String,
    override val domain: String,
    override val port: Int,
    override val value: String
) : RPCAddress {

    companion object {
        private const val serialVersionUID: Long = 413154603904238913L
    }
}