package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/1 14:44
 */
interface Callback<T : Response> {

    fun callback(response: T)
}