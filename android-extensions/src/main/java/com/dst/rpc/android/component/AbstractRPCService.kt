package com.dst.rpc.android.component

import android.app.Service
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
abstract class AbstractRPCService : Service() {

    protected abstract val addressOfCurrentService: AIDLAddress

    protected abstract fun onReceiveRPConnection(context: AIDLContext)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rpContext = intent?.rpcContext ?: return super.onStartCommand(intent, flags, startId)
        if (this.addressOfCurrentService == rpContext.remoteAddress) {
            this.onReceiveRPConnection(context = rpContext)
            AIDLClient.acceptConnection(rpContext.sourceAddress, CompletableDeferred(AIDLConnection(rpContext.callService)))
            AIDLClient.twoWayConnectionEstablish(rpContext.callService)
        }
        return super.onStartCommand(intent, flags, startId)
    }
}