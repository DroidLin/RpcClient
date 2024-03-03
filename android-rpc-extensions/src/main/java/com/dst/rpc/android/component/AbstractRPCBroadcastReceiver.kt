package com.dst.rpc.android.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dst.rpc.android.AIDLRPCAddress
import com.dst.rpc.android.RPContext

/**
 * @author liuzhongao
 * @since 2024/3/2 19:19
 */
abstract class AbstractRPCBroadcastReceiver : BroadcastReceiver() {

    protected abstract val addressOfCurrentReceiver: AIDLRPCAddress

    abstract fun onReceiveRPConnection(context: RPContext)

    override fun onReceive(context: Context?, intent: Intent?) {
        val rpContext = intent?.rpcContext ?: return
        if (this.addressOfCurrentReceiver == rpContext.remoteAddress) {
            this.onReceiveRPConnection(context = rpContext)
            TODO("collect connection")
        }
    }
}