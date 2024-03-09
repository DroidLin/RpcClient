package com.dst.rpc.android

import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard

/**
 * @author liuzhongao
 * @since 2024/3/4 15:54
 */
internal abstract class AndroidCallerCorrelator : AndroidRPCorrelator {

    override fun attachCorrelator(correlator: AndroidRPCorrelator) {}

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = ClientManager.getStubService(functionOwner as Class<INoProguard>)
        .invokeNonSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue)

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = ClientManager.getStubService(functionOwner as Class<INoProguard>)
        .invokeSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue)
}