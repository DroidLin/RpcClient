package com.dst.rpc.android

import com.dst.rpc.Connection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.Continuation

/**
 * android based connection, using aidl component.
 *
 * while launch remote calls, we assume [callService] is always alive.
 * will not handle any exceptions through remote calls.
 *
 * @author liuzhongao
 * @since 2024/3/4 11:13
 */
internal class AIDLConnection(
    private val callService: AndroidCallService
) : Connection {

    override val isClosed: Boolean get() = !this.callService.isOpen

    private val suspendMutex: Mutex = Mutex()

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        return this.suspendMutex.withLock {
            if (isSuspended) {
                this.callService.callSuspendFunction(
                    functionOwner = functionOwner,
                    functionName = functionName,
                    functionUniqueKey = functionUniqueKey,
                    argumentTypes = functionParameterTypes.filter { it != Continuation::class.java },
                    argumentValue = functionParameterValues.filter { it !is Continuation<*> }
                )
            } else {
                this.callService.callFunction(
                    functionOwner = functionOwner,
                    functionName = functionName,
                    functionUniqueKey = functionUniqueKey,
                    argumentTypes = functionParameterTypes,
                    argumentValue = functionParameterValues
                )
            }
        }
    }
}