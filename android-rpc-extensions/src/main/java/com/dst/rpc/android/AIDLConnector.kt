package com.dst.rpc.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dst.rpc.android.component.rpcContext

/**
 * @author liuzhongao
 * @since 2024/3/2 19:52
 */
internal interface AIDLConnector {

    fun attach(androidContext: Context, rpContext: RPContext)

    companion object {
        @JvmStatic
        fun attach(strategy: EstablishStrategy, rpContext: RPContext, androidContext: Context) {
            when (strategy) {
                EstablishStrategy.ContentProvider -> AndroidContentProviderConnector
                EstablishStrategy.BroadcastReceiver -> AndroidBroadcastReceiverConnector
                EstablishStrategy.Service -> AndroidServiceConnector
                else -> throw RuntimeException("unknown EstablishStrategy: ${strategy}")
            }.attach(androidContext, rpContext)
        }
    }
}

private object AndroidServiceConnector : AIDLConnector {
    override fun attach(androidContext: Context, rpContext: RPContext) {
        val broadcastIntent = Intent()
        broadcastIntent.rpcContext = rpContext
        broadcastIntent.`package` = androidContext.packageName
        broadcastIntent.component = ComponentName(androidContext.packageName, rpContext.remoteServiceName)
        androidContext.startService(broadcastIntent)
    }
}

private object AndroidContentProviderConnector : AIDLConnector {

    private const val METHOD = "connection"

    override fun attach(androidContext: Context, rpContext: RPContext) {
        val bundle = Bundle()
        bundle.rpcContext = rpContext
        androidContext.contentResolver.call(rpContext.remoteAddress.uri, METHOD, null, bundle)
    }
}

private object AndroidBroadcastReceiverConnector : AIDLConnector {
    override fun attach(androidContext: Context, rpContext: RPContext) {
        val broadcastIntent = Intent(rpContext.remoteAddress.toString())
        broadcastIntent.rpcContext = rpContext
        broadcastIntent.`package` = androidContext.packageName
        androidContext.sendBroadcast(broadcastIntent)
    }
}