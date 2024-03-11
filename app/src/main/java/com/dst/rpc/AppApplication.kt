package com.dst.rpc

import android.app.ActivityManager
import android.app.Application
import android.content.Context
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

    private val currentProcessName by lazy { this.getCurrentProcessName() }

    override fun onCreate() {
        super.onCreate()
        val processName = this.getCurrentProcessName()
        when {
            !processName.contains(":") -> this.initMainProcess()
            processName.contains("library") -> this.initLibraryProcess()
        }
    }

    private fun initMainProcess() {
        val initConfig = InitConfig.Builder()
            .strategy(EstablishStrategy.BroadcastReceiver)
            .androidContext(this)
            .coroutineContext(EmptyCoroutineContext)
            .build()
        ClientManager.init(initConfig)
    }

    private fun initLibraryProcess() {
        val initConfig = InitConfig.Builder()
            .strategy(EstablishStrategy.BroadcastReceiver)
            .androidContext(this)
            .coroutineContext(EmptyCoroutineContext)
            .build()
        ClientManager.init(initConfig)
//        ClientManager.putService(TestInterface::class.java, TestInterfaceImpl())
    }

    private fun Context.getCurrentProcessName(): String {
        val myPid = android.os.Process.myPid()
        val activityManager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return ""
        return activityManager.runningAppProcesses?.find { it.pid == myPid }?.processName ?: ""
    }
}