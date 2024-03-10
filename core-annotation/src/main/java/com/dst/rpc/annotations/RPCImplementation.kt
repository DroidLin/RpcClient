package com.dst.rpc.annotations

import kotlin.reflect.KClass


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RPCImplementation(val clazz: KClass<*>)
