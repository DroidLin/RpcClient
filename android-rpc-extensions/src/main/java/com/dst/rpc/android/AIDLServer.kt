package com.dst.rpc.android

import android.os.Handler
import android.os.Looper
import com.dst.rpc.Callback
import com.dst.rpc.Request
import com.dst.rpc.Server

/**
 * @author liuzhongao
 * @since 2024/3/1 17:41
 */
internal class AIDLServer : Server<AIDLRequest, AIDLResponse> {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isSupported(request: Request): Boolean = request is AIDLRequest

    override fun execute(request: AIDLRequest, callback: Callback<AIDLResponse>) {
        TODO("Not yet implemented")
    }
}