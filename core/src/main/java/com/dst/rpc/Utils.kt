package com.dst.rpc

import java.lang.reflect.Method
import kotlin.coroutines.Continuation

internal val Method.isSuspendFunction: Boolean
    get() = parameterTypes.lastOrNull() == Continuation::class.java


suspend fun Method.invokeSuspend(instance: Any, vararg args: Any?): Any? {
    return kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
        this.invoke(instance, *args, continuation)
    }
}

internal val defaultReturnType: List<Class<*>> = listOfNotNull(
    Void::class.java,
    Void::class.javaPrimitiveType,
    Unit::class.java,
    Unit::class.javaPrimitiveType
)

internal fun Any?.safeUnbox(): Any? {
    this ?: return null
    if (this.javaClass in defaultReturnType) {
        return null
    }
    return this
}
