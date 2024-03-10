package com.dst.rpc

import com.dst.rpc.annotations.RPCInterface

/**
 * @author liuzhongao
 * @since 2024/3/8 00:53
 */
@RPCInterface
interface TestInterface : INoProguard {

    val name: String

    fun openUserName(number: Int)

    fun openUserNameV2(number: Int): String

    suspend fun suspendOpenUsername(number: Int, string: String)

    suspend fun suspendGetUsername(number: Int): String
}