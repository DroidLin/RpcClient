package com.dst.rpc.socket

import com.dst.rpc.Connection
import com.dst.rpc.CallService

/**
 * @author liuzhongao
 * @since 2024/3/5 20:56
 */
internal class SocketConnection(
    private val callService: CallService,
) : Connection {

    override val isClosed: Boolean get() = !this.callService.isOpen

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        return if (isSuspended) {
            this.callService.callSuspendFunction(
                functionOwner = functionOwner,
                functionName = functionName,
                functionUniqueKey = functionUniqueKey,
                argumentTypes = functionParameterTypes,
                argumentValue = functionParameterValues
            )
        } else this.callService.callFunction(
            functionOwner = functionOwner,
            functionName = functionName,
            functionUniqueKey = functionUniqueKey,
            argumentTypes = functionParameterTypes,
            argumentValue = functionParameterValues
        )
    }
}