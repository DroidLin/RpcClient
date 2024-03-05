package com.dst.rpc.android

import com.dst.rpc.Client
import com.dst.rpc.InitConfig
import com.dst.rpc.RPCAddress

/**
 * factory that accept address like ``` "android://[hostKey]:[port] ```
 */
class AIDLClientFactory : Client.Factory {

    override fun acceptAddress(address: RPCAddress): Boolean {
        return address.scheme == "android"
    }

    override fun addressCreate(value: String): RPCAddress? {
        return if (value.startsWith("android://")) {
            AndroidRPCAddress(value)
        } else null
    }

    override fun newServer(initConfig: InitConfig): Client {
        return AIDLClient(initConfig = initConfig)
    }
}
