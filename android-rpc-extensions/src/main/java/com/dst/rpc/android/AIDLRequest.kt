package com.dst.rpc.android

import com.dst.rpc.Request
import java.io.Serial

/**
 * @author liuzhongao
 * @since 2024/3/1 16:48
 */
internal interface AIDLRequest : Request

internal interface AIDLExtrasRequest : AIDLRequest {
    val rawRequest: AIDLRequest

    fun <T : Any> putExtra(key: String, value: T?)
    fun <T : Any> getExtra(key: String): T?
}

internal val AIDLRequest.rootAIDLRequest: AIDLRequest
    get() {
        var request: AIDLRequest = this
        while (request is AIDLExtrasRequest) {
            request = request.rawRequest
        }
        return request
    }

internal val AIDLExtrasRequest.rootAIDLRequest: AIDLRequest
    get() {
        var request: AIDLRequest = this
        while (request is AIDLExtrasRequest) {
            request = request.rawRequest
        }
        return request
    }

internal fun AIDLExtrasRequest(rawRequest: AIDLRequest): AIDLExtrasRequest = AIDLExtrasRequestImpl(rawRequest)

private class AIDLExtrasRequestImpl(override val rawRequest: AIDLRequest) : AIDLExtrasRequest {

    private val innerMap: MutableMap<String, Any?> = HashMap()

    override fun <T : Any> putExtra(key: String, value: T?) {
        this.innerMap[key] = value
    }

    override fun <T : Any> getExtra(key: String): T? {
        return this.innerMap[key] as? T
    }

    companion object {
        private const val serialVersionUID: Long = -9002238466769876284L
    }

}