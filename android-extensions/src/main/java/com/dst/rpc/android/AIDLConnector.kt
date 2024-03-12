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

    fun attach(androidContext: Context, aidlContext: AIDLContext, preAttachCallback: OnPreAttachCallback? = null)

    companion object {
        @JvmStatic
        @JvmOverloads
        fun attach(strategy: EstablishStrategy, aidlContext: AIDLContext, androidContext: Context, preAttachCallback: OnPreAttachCallback? = null) {
            when (strategy) {
                EstablishStrategy.ContentProvider -> AndroidContentProviderConnector
                EstablishStrategy.BroadcastReceiver -> AndroidBroadcastReceiverConnector
                EstablishStrategy.Service -> AndroidServiceConnector
                else -> throw RuntimeException("unknown EstablishStrategy: $strategy")
            }.attach(androidContext, aidlContext, preAttachCallback)
        }
    }
}

private object AndroidServiceConnector : AIDLConnector {
    override fun attach(androidContext: Context, aidlContext: AIDLContext, preAttachCallback: OnPreAttachCallback?) {
        val serviceLaunchIntent = Intent()
        serviceLaunchIntent.rpcContext = aidlContext
        serviceLaunchIntent.`package` = androidContext.packageName
        serviceLaunchIntent.component = ComponentName(androidContext.packageName, aidlContext.remoteServiceName)
        preAttachCallback?.onPreAttachServiceLaunchIntent(serviceLaunchIntent)
        androidContext.startService(serviceLaunchIntent)
    }
}

private object AndroidContentProviderConnector : AIDLConnector {

    private const val METHOD = "connection"

    override fun attach(androidContext: Context, aidlContext: AIDLContext, preAttachCallback: OnPreAttachCallback?) {
        val bundle = Bundle()
        bundle.rpcContext = aidlContext
        preAttachCallback?.onPreAttachContentProviderBundle(bundle)
        androidContext.contentResolver.call(aidlContext.remoteAddress.uri, METHOD, null, bundle)
    }
}

private object AndroidBroadcastReceiverConnector : AIDLConnector {
    override fun attach(androidContext: Context, aidlContext: AIDLContext, preAttachCallback: OnPreAttachCallback?) {
        val broadcastIntent = Intent(aidlContext.remoteAddress.value)
        broadcastIntent.rpcContext = aidlContext
        broadcastIntent.`package` = androidContext.packageName
        preAttachCallback?.onPreAttachBroadcastIntent(broadcastIntent)
        androidContext.sendBroadcast(broadcastIntent)
    }
}