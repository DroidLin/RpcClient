package com.dst.rpc

import com.dst.rpc.annotation.RPCInterface

/**
 * @author liuzhongao
 * @since 2024/3/8 00:53
 */
@RPCInterface
interface TestInterface {

    val name: String

    fun getUserName(number: Int)

    suspend fun suspendGetUsername(number: Int, string: String)

    suspend fun suspendGetUsername(number: Int): String
}