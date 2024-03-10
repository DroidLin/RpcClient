package com.dst.rpc

import com.dst.rpc.socket.sourceAddress
import kotlinx.coroutines.runBlocking

/**
 * @author liuzhongao
 * @since 2024/3/7 17:25
 */
internal val clientSocketAddress = RPCAddress("socket", "localhost", 12100)

fun main() = runBlocking {
    val initConfig = InitConfig.Builder()
        .sourceAddress(clientSocketAddress)
        .build()
    ClientManager.init(initConfig)
    val testInterface = ClientManager.serviceCreate<TestInterface>(
        clazz = TestInterface::class.java,
        sourceAddress = clientSocketAddress,
        remoteAddress = serverSocketAddress
    )
    for (index in 0 until 10) {
        val startTimestamp = System.nanoTime()
        println(testInterface.suspendGetUsername(1231))
        println("cost: ${(System.nanoTime() - startTimestamp) / 1000_000.0}ms")
    }
}