package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/5 23:39
 */
interface RPCorrelator {

    val isOpen: Boolean get() = false

    fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = ClientManager.getService(functionOwner as Class<INoProguard>)
        .invokeNonSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue).safeUnbox()

    suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = ClientManager.getService(functionOwner as Class<INoProguard>)
        .invokeSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue).safeUnbox()
}
