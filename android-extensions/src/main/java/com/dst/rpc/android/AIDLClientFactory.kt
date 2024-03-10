package com.dst.rpc.android

import com.dst.rpc.Client
import com.dst.rpc.InitConfig
import com.dst.rpc.Address

/**
 * factory that accept address like ``` "android://[hostKey]:[port] ```
 */
class AIDLClientFactory : Client.Factory {

    override fun acceptAddress(address: Address): Boolean {
        return address.scheme == "android"
    }

    override fun addressCreate(value: String): Address? {
        return if (value.startsWith("android://")) {
            AndroidAddress(value)
        } else null
    }

    override fun init(initConfig: InitConfig) {}

    override fun newClient(initConfig: InitConfig): Client {
        return AIDLClient(initConfig = initConfig)
    }
}
