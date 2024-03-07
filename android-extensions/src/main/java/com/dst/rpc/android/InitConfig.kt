package com.dst.rpc.android

import android.app.Service
import android.content.Context
import com.dst.rpc.InitConfig
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author liuzhongao
 * @since 2024/3/2 01:29
 */

private const val KEY_CONNECTION_STRATEGY = "key_connection_strategy"
private const val KEY_ANDROID_CONTEXT = "key_android_context"
private const val KEY_REMOTE_ANDROID_SERVICE_COMPONENT_CLASS = "key_remote_android_service_component_class"
private const val KEY_COROUTINE_CONTEXT = "key_coroutine_context"

val InitConfig.strategy: EstablishStrategy
    get() = requireNotNull(this.extraParameters[KEY_CONNECTION_STRATEGY] as? EstablishStrategy)

fun InitConfig.Builder.strategy(strategy: EstablishStrategy) =
    this.putExtra(KEY_CONNECTION_STRATEGY, strategy)

val InitConfig.androidContext: Context
    get() = requireNotNull(this.extraParameters[KEY_ANDROID_CONTEXT] as? Context)

fun InitConfig.Builder.androidContext(context: Context) =
    this.putExtra(KEY_ANDROID_CONTEXT, context)

val InitConfig.remoteAndroidServiceClass: Class<out Service>?
    get() = this.extraParameters[KEY_REMOTE_ANDROID_SERVICE_COMPONENT_CLASS] as? Class<out Service>

fun InitConfig.Builder.remoteAndroidServiceClass(clazz: Class<out Service>) =
    this.putExtra(KEY_REMOTE_ANDROID_SERVICE_COMPONENT_CLASS, clazz)

val InitConfig.coroutineContext: CoroutineContext
    get() = this.extraParameters[KEY_COROUTINE_CONTEXT] as? CoroutineContext ?: EmptyCoroutineContext

fun InitConfig.Builder.coroutineContext(coroutineContext: CoroutineContext) =
    this.putExtra(KEY_COROUTINE_CONTEXT, coroutineContext)
