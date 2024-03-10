package com.dst.rpc.socket

import com.dst.rpc.Client
import com.dst.rpc.InitConfig
import com.dst.rpc.Address

/**
 * @author liuzhongao
 * @since 2024/3/5 21:59
 */
class SocketClientFactory : Client.Factory {

    override fun acceptAddress(address: Address): Boolean {
        return acceptAddressInner(address)
    }

    override fun addressCreate(value: String): Address? {
        return addressCreateInner(value)
    }

    override fun init(initConfig: InitConfig) {
        SocketFunctionReceiver(initConfig = initConfig).listenToRemoteCall()
    }

    override fun newClient(initConfig: InitConfig): Client {
        return SocketClient(initConfig = initConfig)
    }
}

internal fun acceptAddressInner(address: Address): Boolean {
    return address.scheme == "socket"
}

internal fun addressCreateInner(value: String): Address? {
    return if (value.startsWith("socket://")) {
        SocketAddress(value = value)
    } else null
}