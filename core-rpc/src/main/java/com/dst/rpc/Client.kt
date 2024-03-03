package com.dst.rpc

import java.util.ServiceLoader

/**
 * @author liuzhongao
 * @since 2024/3/1 00:50
 */
interface Client {

    suspend fun openConnection(sourceAddress: RPCAddress, remoteAddress: RPCAddress): Connection

    suspend fun execute(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any?

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

        override suspend fun execute(
            sourceAddress: RPCAddress,
            remoteAddress: RPCAddress,
            functionOwner: Class<*>,
            functionName: String,
            functionParameterTypes: List<Class<*>>,
            functionParameterValues: List<Any?>,
            isSuspended: Boolean
        ): Any? {
            this.collectClientFactories()
            val targetServer = getClient(remoteAddress = remoteAddress)
            return targetServer?.execute(
                sourceAddress = sourceAddress,
                remoteAddress = remoteAddress,
                functionOwner = functionOwner,
                functionName = functionName,
                functionParameterTypes = functionParameterTypes,
                functionParameterValues = functionParameterValues,
                isSuspended = isSuspended,
            )
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

        private fun getClient(remoteAddress: RPCAddress): Client? {
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
            return this.remoteClientImplMap[remoteAddress.value] as? Client
        }
    }
}