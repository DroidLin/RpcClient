package com.dst.rpc

import java.io.Serializable

interface Header : Serializable {
    /**
     * address from which this request comes from.
     */
    val sourceAddress: RPCAddress

    /**
     * address to which this request goes to.
     */
    val remoteAddress: RPCAddress

    companion object : Header {
        private const val serialVersionUID: Long = -2121630231986569072L
        override val sourceAddress: RPCAddress get() = RPCAddress
        override val remoteAddress: RPCAddress get() = RPCAddress
    }
}
