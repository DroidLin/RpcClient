package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/1 00:50
 */
interface Client {

    /**
     * create [Connection] for remote process call, different platforms will have its own implementation.
     */
    fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
    ): Connection

    /**
     * create [Connection] with additional [ExceptionHandler] added, with may handle exceptions before thrown.
     */
    fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        exceptionHandler: ExceptionHandler
    ): Connection

    /**
     * used to create Client accepts [RPCAddress] based on current dependencies.
     */
    interface Factory {

        fun acceptAddress(address: RPCAddress): Boolean

        fun addressCreate(value: String): RPCAddress?

        fun init(initConfig: InitConfig)

        fun newClient(initConfig: InitConfig): Client
    }
}