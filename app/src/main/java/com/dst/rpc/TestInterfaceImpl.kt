package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/10 10:43
 */
class TestInterfaceImpl : TestInterface {
    override val name: String get() = "liuzhongao"

    override fun openUserName(number: Int) {
    }

    override suspend fun suspendOpenUsername(number: Int, string: String) {
    }

    override suspend fun suspendGetUsername(number: Int): String {
        return "liuzhongao$number"
    }
}