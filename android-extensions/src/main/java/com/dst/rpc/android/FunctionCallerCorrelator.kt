package com.dst.rpc.android

import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard

/**
 * @author liuzhongao
 * @since 2024/3/4 15:54
 */
internal abstract class FunctionCallerCorrelator : RPCorrelator {

    override fun attachCorrelator(correlator: RPCorrelator) {}

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val functionOwnerImplementation = ClientManager.getService(functionOwner as Class<INoProguard>)
        return functionOwner.getDeclaredMethod(functionName, *argumentTypes.toTypedArray())
            .invoke(functionOwnerImplementation, *argumentValue.toTypedArray())
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val functionOwnerImplementation = ClientManager.getService(functionOwner as Class<INoProguard>)
        return functionOwner.getDeclaredMethod(functionName, *argumentTypes.toTypedArray())
            .invokeSuspend(functionOwnerImplementation, *argumentValue.toTypedArray())
    }
}