package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.CallService

internal fun interface AIDLCallback : CallService.Callback {

    override fun callback(data: Any?, throwable: Throwable?)
}

internal val AIDLCallback.iBinder: IBinder
    get() = when (this) {
        is AIDLCallbackProxy -> this.aidlFunction.iBinder
        is AIDLCallbackStub -> this.aidlFunction.iBinder
        else -> throw IllegalArgumentException("unknown type of current RPCallback: ${this.javaClass.name}")
    }

internal fun AIDLCallback(aidlFunction: AIDLFunction): AIDLCallback = AIDLCallbackProxy(aidlFunction)

internal fun AIDLCallback(callback: AIDLCallback): AIDLCallback = AIDLCallbackStub(callback)

private class AIDLCallbackProxy(val aidlFunction: AIDLFunction): AIDLCallback {

    override fun callback(data: Any?, throwable: Throwable?) {
        val request = RPCallbackRequest(data = data, throwable = throwable)
        this.aidlFunction.invoke(request = request)
    }
}

private class AIDLCallbackStub(callback: AIDLCallback) : AIDLCallback by callback {
    val aidlFunction = AIDLFunction { request ->
        if (request is RPCallbackRequest) {
            callback(data = request.data, throwable = request.throwable)
        }
        Response
    }
}