package com.dst.rpc.android.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dst.rpc.android.AIDLClient
import com.dst.rpc.android.AIDLConnection
import com.dst.rpc.android.AIDLAddress
import com.dst.rpc.android.AIDLContext
import kotlinx.coroutines.CompletableDeferred

/**
 * @author liuzhongao
 * @since 2024/3/2 19:19
 */
abstract class AbstractRPCBroadcastReceiver : BroadcastReceiver() {

    protected abstract val addressOfCurrentReceiver: AIDLAddress

    protected abstract fun onReceiveRPConnection(context: AIDLContext)

    override fun onReceive(context: Context?, intent: Intent?) {
        val rpContext = intent?.rpcContext ?: return
        if (this.addressOfCurrentReceiver == rpContext.remoteAddress) {
            AIDLClient.acceptConnection(rpContext.sourceAddress, CompletableDeferred(AIDLConnection(rpContext.callService)))
            AIDLClient.twoWayConnectionEstablish(rpContext.callService)
            this.onReceiveRPConnection(context = rpContext)
        }
    }
}