package com.dst.rpc

import java.lang.reflect.Proxy
import java.util.ServiceLoader

/**
 * @author liuzhongao
 * @since 2024/3/5 10:54
 */
object ClientManager {

    private val interfaceImplementationReference = HashMap<Class<*>, INoProguard>()

    private val remoteClientFactoryMap: MutableList<Client.Factory> = ArrayList()
    private val remoteClientImplMap: MutableMap<String, Client> = HashMap()

    private lateinit var initConfig: InitConfig

    @JvmStatic
    fun init(initConfig: InitConfig) {
        this.initConfig = initConfig
        this.collectClientFactories()
        this.initSubModule(initConfig)
    }

    fun addressCreate(value: String): RPCAddress {
        this.collectClientFactories()
        return this.remoteClientFactoryMap.firstNotNullOfOrNull {
            it.addressCreate(value)
        } ?: RPCAddress(value = value)
    }

    fun openConnection(sourceAddress: RPCAddress, remoteAddress: RPCAddress): Connection {
        this.collectClientFactories()
        return this.acquireClient(remoteAddress = remoteAddress)
            .openConnection(sourceAddress, remoteAddress)
    }

    fun openConnection(sourceAddress: RPCAddress, remoteAddress: RPCAddress, exceptionHandler: ExceptionHandler): Connection {
        this.collectClientFactories()
        return this.acquireClient(remoteAddress = remoteAddress)
            .openConnection(sourceAddress, remoteAddress, exceptionHandler)
    }

    /**
     * add implementations of [clazz], which will be invoked in remote process call receiver.
     */
    fun <T : INoProguard> putService(clazz: Class<T>, impl: T) {
        this.interfaceImplementationReference[clazz] = impl
    }

    fun <T : INoProguard> getService(clazz: Class<T>): T {
        return requireNotNull(this.interfaceImplementationReference[clazz] as? T)
    }

    /**
     * create service in client, should pass [sourceAddress] and [remoteAddress] in order to identify who am i and who is there.
     *
     * [exceptionHandler] will handle all exceptions happens through [Connection.call] function.
     * when an exception is not handled by [exceptionHandler], it will be throws at the call point.
     */
    @JvmOverloads
    fun <T : INoProguard> serviceCreate(clazz: Class<*>, sourceAddress: RPCAddress, remoteAddress: RPCAddress, exceptionHandler: ExceptionHandler = ExceptionHandler): T {
        val connection = this.openConnection(sourceAddress, remoteAddress, exceptionHandler)
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), ReflectiveInvocationHandler(connection)) as T
    }

    /**
     * collect client factories with different extension dependencies.
     * using Java-based component SPI to collect and manage.
     */
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

    /**
     * get Client work for [remoteAddress], may throw RuntimeException if there is no factory accept.
     */
    private fun acquireClient(remoteAddress: RPCAddress): Client {
        if (this.remoteClientImplMap[remoteAddress.value] == null) {
            synchronized(this) {
                if (this.remoteClientImplMap[remoteAddress.value] == null) {
                    val factory = synchronized(this) { this.remoteClientFactoryMap.find { it.acceptAddress(address = remoteAddress) } }
                    if (factory != null) {
                        val newGeneratedServerInstance = factory.newClient(initConfig = this.initConfig)
                        this.remoteClientImplMap[remoteAddress.value] = newGeneratedServerInstance
                    }
                }
            }
        }
        return requireNotNull(this.remoteClientImplMap[remoteAddress.value]) {
            "no factory accept address : $remoteAddress"
        }
    }

    private fun initSubModule(initConfig: InitConfig) {
        synchronized(this) {
            for (factory in this.remoteClientFactoryMap) {
                factory.init(initConfig)
            }
        }
    }
}