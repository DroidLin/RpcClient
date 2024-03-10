package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/9 17:34
 */
interface RPCInterfaceRegistry {

    fun <T : INoProguard> putServiceProxyLazy(clazz: Class<T>, factory: (Address, Address, ExceptionHandler) -> T)

    fun <T : INoProguard> putServiceStubLazy(clazz: Class<T>, factory: (T) -> StubFunction)
}