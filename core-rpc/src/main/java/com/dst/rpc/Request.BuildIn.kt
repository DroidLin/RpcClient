package com.dst.rpc

import java.io.Serial

/**
 * @author liuzhongao
 * @since 2024/3/3 12:02
 */
data class InvocationRequest(
    private val sourceAddress: RPCAddress,
    private val hostAddress: RPCAddress,
    val className: String,
    val functionName: String,
    val classTypesOfFunctionParameter: List<String>,
    val valuesOfFunctionParameter: List<Any?>,
) : Request {

    override val header: Header get() = object : Header {
        override val sourceAddress: RPCAddress get() = this@InvocationRequest.sourceAddress
        override val remoteAddress: RPCAddress get() = this@InvocationRequest.hostAddress
    }

    companion object {
        private const val serialVersionUID: Long = -3493071081757263356L
    }
}
