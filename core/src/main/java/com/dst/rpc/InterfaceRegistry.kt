package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/9 17:34
 */
interface InterfaceRegistry {

    fun <T : INoProguard> putServiceProxy(clazz: Class<T>, factory: (Address, Address, ExceptionHandler) -> T)

    fun <T : INoProguard> putServiceStub(clazz: Class<T>, factory: (T) -> StubFunction)

    fun <T : INoProguard> putServiceImpl(clazz: Class<T>, impl: T)
}