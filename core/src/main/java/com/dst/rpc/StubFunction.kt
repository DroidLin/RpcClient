package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/8 01:44
 */
interface StubFunction {

    fun invokeNonSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValue: List<Any?>
    ): Any?

    suspend fun invokeSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValue: List<Any?>
    ): Any?
}

internal fun <T : INoProguard> StubFunction(rawServiceImpl: T): StubFunction = ReflectStubFunction(rawServiceImpl = rawServiceImpl)

private class ReflectStubFunction<T : INoProguard>(private val rawServiceImpl: T) : StubFunction {

    override fun invokeNonSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValue: List<Any?>
    ): Any? {
        return functionOwner.getDeclaredMethod(functionName, *functionParameterTypes.toTypedArray())
            .invoke(this.rawServiceImpl, *functionParameterValue.toTypedArray())
    }

    override suspend fun invokeSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValue: List<Any?>
    ): Any? {
        return functionOwner.getDeclaredMethod(functionName, *functionParameterTypes.toTypedArray())
            .invokeSuspend(this.rawServiceImpl, *functionParameterValue.toTypedArray())
    }
}