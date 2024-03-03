package com.dst.rpc.android

import android.os.IBinder

internal interface RPCallback {

    fun callback(data: Any?, throwable: Throwable?)
}

internal val RPCallback.iBinder: IBinder
    get() = when (this) {
        is RPCallbackProxy -> this.rpcInterface.iBinder
        is RPCallbackStub -> this.rpcInterface.iBinder
        else -> throw IllegalArgumentException("unknown type of current RPCallback: ${this.javaClass.name}")
    }

internal fun RPCallback(rpcInterface: RPCInterface): RPCallback = RPCallbackProxy(rpcInterface)

private typealias RPCallbackStubInnerBlock = (data: Any?, throwable: Throwable?) -> Unit

internal fun RPCallback(callback: RPCallbackStubInnerBlock): RPCallback = RPCallbackStub(callback)

private class RPCallbackProxy(val rpcInterface: RPCInterface): RPCallback {
    override fun callback(data: Any?, throwable: Throwable?) {
        val request = RPCallbackRequest(data = data, throwable = throwable)
        rpcInterface.invoke(request = request)
    }
}

private class RPCallbackStub(private val block: RPCallbackStubInnerBlock) : RPCallback {
    val rpcInterface = RPCInterface { request ->
        if (request is RPCallbackRequest) {
            callback(data = request.data, throwable = request.throwable)
        }
        AIDLResponse
    }

    override fun callback(data: Any?, throwable: Throwable?) {
        this.block(data, throwable)
    }
}