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

    fun attach(androidContext: Context, AIDLContext: AIDLContext)

    companion object {
        @JvmStatic
        fun attach(strategy: EstablishStrategy, AIDLContext: AIDLContext, androidContext: Context) {
            when (strategy) {
                EstablishStrategy.ContentProvider -> AndroidContentProviderConnector
                EstablishStrategy.BroadcastReceiver -> AndroidBroadcastReceiverConnector
                EstablishStrategy.Service -> AndroidServiceConnector
                else -> throw RuntimeException("unknown EstablishStrategy: ${strategy}")
            }.attach(androidContext, AIDLContext)
        }
    }
}

private object AndroidServiceConnector : AIDLConnector {
    override fun attach(androidContext: Context, AIDLContext: AIDLContext) {
        val broadcastIntent = Intent()
        broadcastIntent.rpcContext = AIDLContext
        broadcastIntent.`package` = androidContext.packageName
        broadcastIntent.component = ComponentName(androidContext.packageName, AIDLContext.remoteServiceName)
        androidContext.startService(broadcastIntent)
    }
}

private object AndroidContentProviderConnector : AIDLConnector {

    private const val METHOD = "connection"

    override fun attach(androidContext: Context, AIDLContext: AIDLContext) {
        val bundle = Bundle()
        bundle.rpcContext = AIDLContext
        androidContext.contentResolver.call(AIDLContext.remoteAddress.uri, METHOD, null, bundle)
    }
}

private object AndroidBroadcastReceiverConnector : AIDLConnector {
    override fun attach(androidContext: Context, AIDLContext: AIDLContext) {
        val broadcastIntent = Intent(AIDLContext.remoteAddress.value)
        broadcastIntent.rpcContext = AIDLContext
        broadcastIntent.`package` = androidContext.packageName
        androidContext.sendBroadcast(broadcastIntent)
    }
}