package com.dst.rpc.socket

import com.dst.rpc.Client
import com.dst.rpc.InitConfig
import com.dst.rpc.RPCAddress

/**
 * @author liuzhongao
 * @since 2024/3/5 21:59
 */
class SocketClientFactory : Client.Factory {

    override fun acceptAddress(address: RPCAddress): Boolean {
        return address.scheme == "socket"
    }

    override fun addressCreate(value: String): RPCAddress? {
        return if (value.startsWith("socket://")) {
            SocketRPCAddress(value = value)
        } else null
    }

    override fun init(initConfig: InitConfig) {
        SocketFunctionReceiver(initConfig = initConfig).listenToRemoteCall()
    }

    override fun newClient(initConfig: InitConfig): Client {
        return SocketClient(initConfig = initConfig)
    }
}