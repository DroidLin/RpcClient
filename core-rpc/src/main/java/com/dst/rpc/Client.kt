package com.dst.rpc

import java.util.ServiceLoader

/**
 * @author liuzhongao
 * @since 2024/3/1 00:50
 */
interface Client {

    suspend fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
    ): Connection

    suspend fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        exceptionHandler: ExceptionHandler
    ): Connection

    /**
     * used to create Server based on current dependencies.
     */
    interface Factory {

        fun acceptAddress(address: RPCAddress): Boolean

        fun newServer(initConfig: InitConfig): Client
    }

    companion object : Client {

        private val remoteClientFactoryMap: MutableList<Client.Factory> = ArrayList()
        private val remoteClientImplMap: MutableMap<String, Client> = HashMap()

        private lateinit var initConfig: InitConfig

        @JvmStatic
        fun init(initConfig: InitConfig) {
            this.initConfig = initConfig
        }

        override suspend fun openConnection(
            sourceAddress: RPCAddress,
            remoteAddress: RPCAddress
        ): Connection {
            this.collectClientFactories()
            return this.acquireClient(remoteAddress = remoteAddress).openConnection(sourceAddress, remoteAddress)
        }

        override suspend fun openConnection(
            sourceAddress: RPCAddress,
            remoteAddress: RPCAddress,
            exceptionHandler: ExceptionHandler
        ): Connection {
            this.collectClientFactories()
            return this.acquireClient(remoteAddress = remoteAddress).openConnection(sourceAddress, remoteAddress, exceptionHandler)
        }

        private fun collectClientFactories() {
            if (this.remoteClientFactoryMap.isEmpty()) {
                synchronized(this) {
                    if (this.remoteClientFactoryMap.isEmpty()) {
                        val tempFactoryList = ServiceLoader.load(Client.Factory::class.java).toList()
                        this.remoteClientFactoryMap += tempFactoryList
                    }
                }
            }
            if (this.remoteClientFactoryMap.isEmpty()) {
                throw IllegalStateException("No server implementation found.")
            }
        }

        private fun acquireClient(remoteAddress: RPCAddress): Client {
            if (this.remoteClientImplMap[remoteAddress.value] == null) {
                synchronized(this) {
                    if (this.remoteClientImplMap[remoteAddress.value] == null) {
                        val factory = synchronized(this) { this.remoteClientFactoryMap.find { it.acceptAddress(address = remoteAddress) } }
                        if (factory != null) {
                            val newGeneratedServerInstance = factory.newServer(initConfig = this.initConfig)
                            this.remoteClientImplMap[remoteAddress.value] = newGeneratedServerInstance
                        }
                    }
                }
            }
            return requireNotNull(this.remoteClientImplMap[remoteAddress.value]) {
                "no factory accept address : $remoteAddress"
            }
        }
    }
}