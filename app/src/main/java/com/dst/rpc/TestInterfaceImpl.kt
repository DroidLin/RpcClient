package com.dst.rpc

import android.util.Log
import com.dst.rpc.annotations.RPCImplFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author liuzhongao
 * @since 2024/3/10 10:43
 */
//@RPCImplementation(clazz = TestInterface::class)
class TestInterfaceImpl : TestInterface {
    override val name: String get() {
        Log.i("LZAAAAAAA", "receive getName")
        return "liuzhongao"
    }

    override fun openUserName(number: Int) {
    }

    override fun openUserNameV2(number: Int): String {
        return ""
    }

    override suspend fun suspendOpenUsername(number: Int, string: String) {
    }

    override suspend fun suspendGetUsername(number: Int): String {
        return withContext(Dispatchers.IO) {
            "liuzhongao$number"
        }
    }

    @RPCImplFactory(clazz = TestInterface::class)
    class Factory : InterfaceFactory<TestInterface> {
        override fun interfaceCreate(): TestInterface {
            return TestInterfaceImpl()
        }
    }
}