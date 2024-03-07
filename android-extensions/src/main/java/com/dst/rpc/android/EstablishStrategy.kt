package com.dst.rpc.android

/**
 * connection type for android rpc extensions.
 */
enum class EstablishStrategy {
    ContentProvider,
    Service,
    BroadcastReceiver
}