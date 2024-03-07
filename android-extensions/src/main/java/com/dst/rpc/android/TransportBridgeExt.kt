package com.dst.rpc.android

private const val KEY_REQUEST = "KEY_REQUEST"
private const val KEY_RESPONSE = "KEY_RESPONSE"


internal var TransportBridge.request: Request?
    set(value) { this.innerParameterMap[KEY_REQUEST] = value }
    get() = this.innerParameterMap[KEY_REQUEST] as? Request

internal var TransportBridge.response: Response?
    set(value) { this.innerParameterMap[KEY_RESPONSE] = value }
    get() = this.innerParameterMap[KEY_RESPONSE] as? Response