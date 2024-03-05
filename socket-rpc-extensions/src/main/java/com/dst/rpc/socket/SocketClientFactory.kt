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
        TODO("Not yet implemented")
    }

    override fun addressCreate(value: String): RPCAddress? {
        TODO("Not yet implemented")
    }

    override fun newServer(initConfig: InitConfig): Client {
        TODO("Not yet implemented")
    }
}