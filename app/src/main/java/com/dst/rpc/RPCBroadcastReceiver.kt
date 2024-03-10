package com.dst.rpc

import com.dst.rpc.android.AIDLAddress
import com.dst.rpc.android.AndroidAddress
import com.dst.rpc.android.AIDLContext
import com.dst.rpc.android.component.AbstractRPCBroadcastReceiver

/**
 * @author liuzhongao
 * @since 2024/3/10 11:11
 */
class RPCBroadcastReceiver : AbstractRPCBroadcastReceiver() {
    override val addressOfCurrentReceiver: AIDLAddress = AndroidAddress(libraryProcessAddress)

    override fun onReceiveRPConnection(context: AIDLContext) {
    }
}