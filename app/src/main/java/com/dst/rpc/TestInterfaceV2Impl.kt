package com.dst.rpc

import com.dst.rpc.annotations.RPCImplementation

/**
 * @author liuzhongao
 * @since 2024/3/11 15:43
 */
@RPCImplementation(clazz = TestInterfaceV2::class)
class TestInterfaceV2Impl : TestInterfaceV2 {

    override val String.name: String
        get() = TODO("Not yet implemented")

    override fun Int.getUserName(number: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun String.suspendGetUsername(number: Int, string: String) {
        TODO("Not yet implemented")
    }

    override suspend fun Long.suspendGetUsername(number: Int): String {
        TODO("Not yet implemented")
    }
}