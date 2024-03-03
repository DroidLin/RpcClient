package com.dst.rpc.android

import com.dst.rpc.Server

/**
 * @author liuzhongao
 * @since 2024/3/2 14:54
 */
internal class AIDLServer : Server<AIDLRequest, AIDLResponse> {

    val rpcInterface = RPCInterface { request ->
        invoke(request as AIDLRequest)
    }

    override fun invoke(request: AIDLRequest): AIDLResponse {
        TODO("Not yet implemented")
    }
}