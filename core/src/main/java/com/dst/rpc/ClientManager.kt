package com.dst.rpc

import java.lang.reflect.Proxy
import java.util.ServiceLoader

/**
 * @author liuzhongao
 * @since 2024/3/5 10:54
 */
object ClientManager {

    private val interfaceImplementationReference = HashMap<Class<*>, INoProguard>()

    private val interfaceProxyFactories: MutableMap<Class<*>, (RPCAddress, RPCAddress, ExceptionHandler) -> INoProguard> = HashMap()
    private val interfaceStubFactories: MutableMap<Class<*>, (INoProguard) -> StubFunction> = HashMap()

    private val interfaceStubFunctionCache: MutableMap<Class<*>, StubFunction> = HashMap()

    private val remoteClientFactoryMap: MutableList<Client.Factory> = ArrayList()
    private val remoteClientImplMap: MutableMap<String, Client> = HashMap()

    private lateinit var initConfig: InitConfig

    @JvmStatic
    fun init(initConfig: InitConfig) {
        this.initConfig = initConfig
        this.collectClientFactories()
        this.collectCodeGenerationImpl()
        this.initSubModule(initConfig)
    }

    @JvmStatic
    fun addressCreate(value: String): RPCAddress {
        this.collectClientFactories()
        return this.remoteClientFactoryMap.firstNotNullOfOrNull {
            it.addressCreate(value)
        } ?: RPCAddress(value = value)
    }

    @JvmStatic
    @JvmOverloads
    fun openConnection(sourceAddress: RPCAddress, remoteAddress: RPCAddress, exceptionHandler: ExceptionHandler = ExceptionHandler): Connection {
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

    fun getStubService(clazz: Class<*>): StubFunction {
        val rawService = this.interfaceImplementationReference[clazz]
            ?: throw NullPointerException("no business impl for interface class: ${clazz.name} is available.")

        if (this.interfaceStubFunctionCache[clazz] == null) {
            synchronized(this.interfaceStubFunctionCache) {
                if (this.interfaceStubFunctionCache[clazz] == null) {
                    val factory = this.interfaceStubFactories[clazz]
                    val stubFunction = factory?.invoke(rawService)
                    this.interfaceStubFunctionCache[clazz] = stubFunction ?: StubFunction(rawServiceImpl = rawService)
                }
            }
        }
        return requireNotNull(this.interfaceStubFunctionCache[clazz])
    }

    /**
     * create service in client, should pass [sourceAddress] and [remoteAddress] in order to identify who am i and who is there.
     *
     * [exceptionHandler] will handle all exceptions happens through [Connection.call] function.
     * when an exception is not handled by [exceptionHandler], it will be throws at the call point.
     */
    @JvmOverloads
    fun <T : INoProguard> serviceCreate(clazz: Class<*>, sourceAddress: RPCAddress, remoteAddress: RPCAddress, exceptionHandler: ExceptionHandler = ExceptionHandler): T {
        val factory = this.interfaceProxyFactories[clazz]
        if (factory != null) {
            return factory.invoke(sourceAddress, remoteAddress, exceptionHandler) as T
        }

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
     * collect auto-generated impl using Java-base Component(SPI), may not the best choice to collect,
     * but is the best platform-based way to adapt all platforms implementations.
     */
    private fun collectCodeGenerationImpl() {
        val isProxyFactoriesEmpty = { this.interfaceProxyFactories.isEmpty() }
        val isStubFactoriesEmpty = { this.interfaceStubFactories.isEmpty() }
        if (isProxyFactoriesEmpty() || isStubFactoriesEmpty()) {
            synchronized(this) {
                if (isProxyFactoriesEmpty() || isStubFactoriesEmpty()) {
                    val registry = object : RPCInterfaceRegistry {
                        override fun <T : INoProguard> putServiceProxyLazy(clazz: Class<T>, factory: (RPCAddress, RPCAddress, ExceptionHandler) -> T) {
                            this@ClientManager.interfaceProxyFactories[clazz] = factory
                        }

                        override fun <T : INoProguard> putServiceStubLazy(clazz: Class<T>, factory: (T) -> StubFunction) {
                            this@ClientManager.interfaceStubFactories[clazz] = factory as (INoProguard) -> StubFunction
                        }
                    }
                    ServiceLoader.load(RPCollector::class.java).forEach { it.collect(registry) }
                }
            }
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