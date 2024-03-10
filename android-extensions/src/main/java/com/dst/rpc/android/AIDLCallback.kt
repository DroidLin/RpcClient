package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.CallService

internal fun interface AIDLCallback : CallService.Callback {

    override fun callback(data: Any?, throwable: Throwable?)
}

internal val AIDLCallback.iBinder: IBinder
    get() = when (this) {
        is AIDLCallbackProxy -> this.rpcInterface.iBinder
        is AIDLCallbackStub -> this.rpcInterface.iBinder
        else -> throw IllegalArgumentException("unknown type of current RPCallback: ${this.javaClass.name}")
    }

internal fun AIDLCallback(rpcInterface: RPCInterface): AIDLCallback = AIDLCallbackProxy(rpcInterface)

internal fun AIDLCallback(callback: AIDLCallback): AIDLCallback = AIDLCallbackStub(callback)

private class AIDLCallbackProxy(val rpcInterface: RPCInterface): AIDLCallback {
    override fun callback(data: Any?, throwable: Throwable?) {
        val request = RPCallbackRequest(data = data, throwable = throwable)
        rpcInterface.invoke(request = request)
    }
}

private class AIDLCallbackStub(callback: AIDLCallback) : AIDLCallback by callback {
    val rpcInterface = RPCInterface { request ->
        if (request is RPCallbackRequest) {
            callback(data = request.data, throwable = request.throwable)
        }
        Response
    }
}