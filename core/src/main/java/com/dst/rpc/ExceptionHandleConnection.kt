package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/4 14:25
 */
class ExceptionHandleConnection(
    private val exceptionHandler: ExceptionHandler,
    /**
     * find the latest connection for remote process call, may suspend while connection is not established.
     */
    private val rawConnectionProvider: suspend () -> Connection
) : Connection {

    override val isClosed: Boolean get() = true

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        val result = kotlin.runCatching {
            this.rawConnectionProvider.invoke().call(
                functionOwner = functionOwner,
                functionName = functionName,
                functionUniqueKey = functionUniqueKey,
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