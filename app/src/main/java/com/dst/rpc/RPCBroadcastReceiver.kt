package com.dst.rpc

import com.dst.rpc.android.AIDLRPCAddress
import com.dst.rpc.android.AndroidRPCAddress
import com.dst.rpc.android.RPContext
import com.dst.rpc.android.component.AbstractRPCBroadcastReceiver

/**
 * @author liuzhongao
 * @since 2024/3/10 11:11
 */
class RPCBroadcastReceiver : AbstractRPCBroadcastReceiver() {
    override val addressOfCurrentReceiver: AIDLRPCAddress = AndroidRPCAddress(libraryProcessAddress)

    override fun onReceiveRPConnection(context: RPContext) {
    }
}