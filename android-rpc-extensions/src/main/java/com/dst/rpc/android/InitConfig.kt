package com.dst.rpc.android

import android.app.Service
import android.content.Context
import com.dst.rpc.InitConfig
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author liuzhongao
 * @since 2024/3/2 01:29
 */

val InitConfig.strategy: EstablishStrategy
    get() = requireNotNull(this.extraParameters["connectionStrategy"] as? EstablishStrategy)

fun InitConfig.Builder.strategy(strategy: EstablishStrategy) {
    this.putExtra("connectionStrategy", strategy)
}

val InitConfig.androidContext: Context
    get() = requireNotNull(this.extraParameters["androidContext"] as? Context)

fun InitConfig.Builder.androidContext(context: Context) {
    this.putExtra("androidContext", context)
}

val InitConfig.remoteAndroidServiceClass: Class<out Service>?
    get() = this.extraParameters["remoteAndroidServiceClass"] as? Class<out Service>

fun InitConfig.Builder.remoteAndroidServiceClass(clazz: Class<out Service>) {
    this.putExtra("androidContext", clazz)
}

val InitConfig.coroutineContext: CoroutineContext
    get() = this.extraParameters["coroutineContext"] as? CoroutineContext ?: EmptyCoroutineContext

fun InitConfig.Builder.coroutineContext(coroutineContext: CoroutineContext) {
    this.putExtra("coroutineContext", coroutineContext)
}
