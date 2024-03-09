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
