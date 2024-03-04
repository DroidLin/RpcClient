package com.dst.rpc.android

import com.dst.rpc.Connection
import com.dst.rpc.ExceptionHandler

/**
 * @author liuzhongao
 * @since 2024/3/4 14:25
 */
internal class ExceptionHandleConnection(
    private val exceptionHandler: ExceptionHandler,
    private val rawConnectionFetcher: suspend () -> Connection
) : Connection {

    override val isClosed: Boolean get() = true

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        val result = kotlin.runCatching {
            this.rawConnectionFetcher.invoke().call(
                functionOwner = functionOwner,
                functionName = functionName,
                functionParameterTypes = functionParameterTypes,
                functionParameterValues = functionParameterValues,
                isSuspended = isSuspended
            )
        }
        val throwable = result.exceptionOrNull()
        if (result.isFailure && throwable != null) {
            if (this.exceptionHandler.handle(throwable = throwable)) {
                return null
            }
            throw UnHandledRuntimeException(throwable)
        }
        return result.getOrNull()
    }
}