package com.dst.rpc.android

import com.dst.rpc.Response
import java.io.Serial

/**
 * @author liuzhongao
 * @since 2024/3/1 17:07
 */
interface AIDLResponse : Response {

    companion object : AIDLResponse {
        private const val serialVersionUID: Long = 8651218292810629022L
        override val data: Any? get() = null
        override val throwable: Throwable? get() = null
    }
}