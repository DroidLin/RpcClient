package com.dst.rpc.socket

import com.dst.rpc.Connection
import java.net.Socket

/**
 * @author liuzhongao
 * @since 2024/3/5 20:56
 */
internal class SocketConnection(
    private val rpCorrelator: RPCorrelator,
) : Connection {

    override val isClosed: Boolean get() = !this.rpCorrelator.isOpen

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        return if (isSuspended) {
            this.rpCorrelator.callSuspendFunction(
                functionOwner = functionOwner,
                functionName = functionName,
                argumentTypes = functionParameterTypes,
                argumentValue = functionParameterValues
            )
        } else this.rpCorrelator.callFunction(
            functionOwner = functionOwner,
            functionName = functionName,
            argumentTypes = functionParameterTypes,
            argumentValue = functionParameterValues
        )
    }
}