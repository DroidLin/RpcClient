package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/9 16:56
 */
interface Collector {

    fun collect(registry: RPCInterfaceRegistry)
}