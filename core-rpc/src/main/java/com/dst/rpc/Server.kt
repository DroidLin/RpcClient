package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/2 14:52
 */
interface Server<P : Request, T : Response> {

    fun invoke(request: P): T
}