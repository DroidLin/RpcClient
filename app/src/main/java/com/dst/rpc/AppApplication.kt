package com.dst.rpc

import android.app.Application
import com.dst.rpc.android.EstablishStrategy
import com.dst.rpc.android.androidContext
import com.dst.rpc.android.coroutineContext
import com.dst.rpc.android.strategy
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author liuzhongao
 * @since 2024/3/10 11:14
 */
class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val initConfig = InitConfig.Builder()
            .strategy(EstablishStrategy.BroadcastReceiver)
            .androidContext(this)
            .coroutineContext(EmptyCoroutineContext)
            .build()
        ClientManager.init(initConfig)
    }
}