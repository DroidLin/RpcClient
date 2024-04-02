package com.dst.rpc.annotations

import kotlin.reflect.KClass

/**
 * @author liuzhongao
 * @since 2024/4/2 11:13
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RPCImplFactory(val clazz: KClass<*>)
