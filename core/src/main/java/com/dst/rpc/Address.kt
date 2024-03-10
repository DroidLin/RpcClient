package com.dst.rpc

import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/3 14:46
 */
interface Address : Serializable {
    val scheme: String
    val domain: String
    val port: Int
    val value: String get() = ""

    companion object : Address {
        private const val serialVersionUID: Long = 5562944500009031836L
        override val scheme: String get() = ""
        override val domain: String get() = ""
        override val port: Int get() = -1
    }
}

@JvmOverloads
fun Address(scheme: String = "", domain: String = "", port: Int = -1, value: String? = null): Address {
    val rawValue = value ?: if (port > 0) {
        "${scheme}://${domain}:${port}"
    } else "${scheme}://${domain}"
    return AddressImpl(scheme, domain, port, rawValue)
}

private class AddressImpl(
    override val scheme: String,
    override val domain: String,
    override val port: Int,
    override val value: String
) : Address {

    companion object {
        private const val serialVersionUID: Long = 413154603904238913L
    }
}