package com.dst.rpc.socket

import com.dst.rpc.*

/**
 * @author liuzhongao
 * @since 2024/3/5 20:55
 */
internal class SocketClient(initConfig: InitConfig) : Client {

    private val rootExceptionHandler: ExceptionHandler = initConfig.rootExceptionHandler
    private val connectionTimeout: Long = initConfig.connectTimeout

    override fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress
    ): Connection = this.openConnection(sourceAddress, remoteAddress) { false }

    override fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        exceptionHandler: ExceptionHandler
    ): Connection {
        TODO("Not yet implemented")
    }
}