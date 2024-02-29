package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/1 00:50
 */
interface Server {

    fun execute(request: Request): Response
}