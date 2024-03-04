package com.dst.rpc.android

import android.os.Looper
import java.lang.reflect.Method

val isMainThread: Boolean get() = Looper.getMainLooper() == Looper.myLooper()

fun assertWorkerThread() { require(!isMainThread) }

fun assertMainThread() { require(isMainThread) }

internal val Array<String>.stringTypeConvert: Array<Class<*>>
    get() = this.map { className -> className.stringTypeConvert }.toTypedArray()

internal val List<String>.stringTypeConvert: List<Class<*>>
    get() = this.map { className -> className.stringTypeConvert }

internal val String.stringTypeConvert: Class<*>
    get() = when (this) {
        Byte::class.java.name -> Byte::class.java
        Int::class.java.name -> Int::class.java
        Short::class.java.name -> Short::class.java
        Long::class.java.name -> Long::class.java
        Float::class.java.name -> Float::class.java
        Double::class.java.name -> Double::class.java
        Boolean::class.java.name -> Boolean::class.java
        Char::class.java.name -> Char::class.java
        else -> Class.forName(this)
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

internal suspend fun Method.invokeSuspend(instance: Any, vararg args: Any?): Any? {
    return kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { continuation ->
        this.invoke(instance, *args, continuation)
    }
}
