package com.dst.rpc.android.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dst.rpc.android.AIDLClient
import com.dst.rpc.android.AIDLConnection
import com.dst.rpc.android.AIDLRPCAddress
import com.dst.rpc.android.RPContext
import kotlinx.coroutines.CompletableDeferred

/**
 * @author liuzhongao
 * @since 2024/3/2 19:19
 */
abstract class AbstractRPCBroadcastReceiver : BroadcastReceiver() {

    protected abstract val addressOfCurrentReceiver: AIDLRPCAddress

    protected abstract fun onReceiveRPConnection(context: RPContext)

    override fun onReceive(context: Context?, intent: Intent?) {
        val rpContext = intent?.rpcContext ?: return
        if (this.addressOfCurrentReceiver == rpContext.remoteAddress) {
            this.onReceiveRPConnection(context = rpContext)
            AIDLClient.acceptConnection(rpContext.sourceAddress, CompletableDeferred(AIDLConnection(rpContext.rpCorrelator)))
            AIDLClient.twoWayConnectionEstablish(rpContext.rpCorrelator)
        }
    }
}