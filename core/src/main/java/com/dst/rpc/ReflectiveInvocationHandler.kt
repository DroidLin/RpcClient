package com.dst.rpc

import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

/**
 * @author liuzhongao
 * @since 2024/3/5 10:31
 */
internal class ReflectiveInvocationHandler(
    private val connection: Connection
) : InvocationHandler {
    override fun invoke(p0: Any?, p1: Method?, p2: Array<Any?>?): Any? {
        requireNotNull(p1) { "incorrect method, please check." }
        val functionOwner = p1.declaringClass
        val functionName = p1.name
        val functionParameterTypes = p1.parameterTypes.toList().filterNotNull()
        val functionParameterValues = p2?.filter { it !is Continuation<*> } ?: emptyList()
        val isSuspendFunction = functionParameterTypes.lastOrNull() == Continuation::class.java
        return if (isSuspendFunction) {
            val continuation = requireNotNull(p2?.find { it is Continuation<*> } as Continuation<Any?>)
            val functionParameterTypesWithoutContinuation = functionParameterTypes.filter { it.javaClass != Continuation::class.java }
            (this.connection::call as Function7<Class<*>, String, String, List<Class<*>>, List<Any?>, Boolean, Continuation<Any?>, Any?>)
                .invoke(functionOwner, functionName, "", functionParameterTypesWithoutContinuation, functionParameterValues, true, continuation)
        } else runBlocking { this@ReflectiveInvocationHandler.connection.call(functionOwner, functionName, "", functionParameterTypes, functionParameterValues, false) }
    }
}