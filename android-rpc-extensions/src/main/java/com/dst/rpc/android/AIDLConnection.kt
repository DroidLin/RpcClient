package com.dst.rpc.android

import com.dst.rpc.Connection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.Continuation

/**
 * @author liuzhongao
 * @since 2024/3/4 11:13
 */
internal class AIDLConnection(
    private val rpCorrelator: RPCorrelator
) : Connection {

    override val isClosed: Boolean get() = !this.rpCorrelator.isOpen

    private val suspendMutex: Mutex = Mutex()

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        return this.suspendMutex.withLock {
            if (isSuspended) {
                this.rpCorrelator.callSuspendFunction(
                    functionOwner = functionOwner,
                    functionName = functionName,
                    argumentTypes = functionParameterTypes.filter { it != Continuation::class.java },
                    argumentValue = functionParameterValues.filter { it !is Continuation<*> }
                )
            } else {
                this.rpCorrelator.callFunction(
                    functionOwner = functionOwner,
                    functionName = functionName,
                    argumentTypes = functionParameterTypes,
                    argumentValue = functionParameterValues
                )
            }
        }
    }
}