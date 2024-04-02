package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/4/2 11:23
 */
fun interface InterfaceFactory<T : INoProguard> {

    fun interfaceCreate(): T
}