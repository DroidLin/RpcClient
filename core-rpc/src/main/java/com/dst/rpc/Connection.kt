package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/3 23:25
 */
interface Connection {

    val isClosed: Boolean

    suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any?
}