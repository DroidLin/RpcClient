package com.dst.rpc

import java.lang.reflect.Method
import kotlin.coroutines.Continuation

internal val Method.isSuspendFunction: Boolean
    get() = parameterTypes.lastOrNull() == Continuation::class.java