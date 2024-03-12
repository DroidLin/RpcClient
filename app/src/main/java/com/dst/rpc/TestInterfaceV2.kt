package com.dst.rpc

import com.dst.rpc.annotations.RPCInterface

/**
 * @author liuzhongao
 * @since 2024/3/9 18:48
 */
@RPCInterface
interface TestInterfaceV2 : INoProguard {

    val name: String get() = ""

    val String.name: String

    fun Int.getUserName(number: Int)

    suspend fun String.suspendGetUsername(number: Int, string: String)

    suspend fun Long.suspendGetUsername(number: Int): String
}