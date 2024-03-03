package com.dst.rpc

import java.io.Serial
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/1 00:51
 */
interface Request : Serializable {

    companion object : Request {
        private const val serialVersionUID: Long = 6436168246180506461L
    }
}