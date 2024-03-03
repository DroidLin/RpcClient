package com.dst.rpc.android

import com.dst.rpc.InitConfig

/**
 * connection type for android rpc extensions.
 */
enum class EstablishStrategy {
    ContentProvider,
    Service,
    BroadcastReceiver
}