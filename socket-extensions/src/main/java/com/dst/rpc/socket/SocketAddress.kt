package com.dst.rpc.socket

import com.dst.rpc.Address
import java.net.URI

/**
 * @author liuzhongao
 * @since 2024/3/6 23:33
 */
internal class SocketAddress(uri: URI) : Address {

    constructor(value: String) : this(URI.create(value))

    override val scheme: String by lazy { uri.scheme }
    override val domain: String by lazy { uri.host }
    override val port: Int by lazy { uri.port }

    companion object {
        private const val serialVersionUID: Long = 6125666403161913689L
    }
}