package com.dst.rpc

/**
 * entrance to invoke remote process function.
 *
 * @author liuzhongao
 * @since 2024/3/3 23:25
 */
interface Connection {

    val isClosed: Boolean

    /**
     * real call to remote, should aware that [functionParameterTypes] will not contain
     * [kotlin.coroutines.Continuation] class type and so does [functionParameterValues] .
     */
    suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any?
}