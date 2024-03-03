package com.dst.rpc.android.component

import android.app.Service
import android.content.Intent
import com.dst.rpc.android.AIDLRPCAddress
import com.dst.rpc.android.RPContext

/**
 * @author liuzhongao
 * @since 2024/3/2 19:19
 */
abstract class AbstractRPCService : Service() {

    protected abstract val addressOfCurrentService: AIDLRPCAddress

    abstract fun onReceiveRPConnection(context: RPContext)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rpContext = intent?.rpcContext ?: return super.onStartCommand(intent, flags, startId)
        if (this.addressOfCurrentService == rpContext.remoteAddress) {
            this.onReceiveRPConnection(context = rpContext)
            TODO("collect connection")
        }
        return super.onStartCommand(intent, flags, startId)
    }
}