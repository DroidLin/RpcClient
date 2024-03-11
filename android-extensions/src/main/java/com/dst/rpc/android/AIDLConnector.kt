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
    override fun attach(androidContext: Context, aidlContext: AIDLContext) {
        val broadcastIntent = Intent()
        broadcastIntent.rpcContext = aidlContext
        broadcastIntent.`package` = androidContext.packageName
        broadcastIntent.component = ComponentName(androidContext.packageName, aidlContext.remoteServiceName)
        androidContext.startService(broadcastIntent)
    }
}

private object AndroidContentProviderConnector : AIDLConnector {

    private const val METHOD = "connection"

    override fun attach(androidContext: Context, aidlContext: AIDLContext) {
        val bundle = Bundle()
        bundle.rpcContext = aidlContext
        androidContext.contentResolver.call(aidlContext.remoteAddress.uri, METHOD, null, bundle)
    }
}

private object AndroidBroadcastReceiverConnector : AIDLConnector {
    override fun attach(androidContext: Context, aidlContext: AIDLContext) {
        val broadcastIntent = Intent(aidlContext.remoteAddress.value)
        broadcastIntent.rpcContext = aidlContext
        broadcastIntent.`package` = androidContext.packageName
        androidContext.sendBroadcast(broadcastIntent)
    }
}