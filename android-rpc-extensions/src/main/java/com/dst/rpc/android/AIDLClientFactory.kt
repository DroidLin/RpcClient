package com.dst.rpc.android

import com.dst.rpc.Client
import com.dst.rpc.Connection
import com.dst.rpc.InitConfig
import com.dst.rpc.RPCAddress
import kotlinx.coroutines.CompletableDeferred

/**
 * factory that accept address like ``` "android://[hostKey]:[port] ```
 */
class AIDLClientFactory : Client.Factory {

    override fun collectConnection(address: RPCAddress, connection: Connection) {
        AIDLClient.acceptConnectionTask(address, CompletableDeferred(connection))
    }

    override fun acceptAddress(address: RPCAddress): Boolean {
        return address.scheme == "android"
    }

    override fun newServer(initConfig: InitConfig): Client {
        return AIDLClient(initConfig = initConfig)
    }
}
