package com.dst.rpc.android

import com.dst.rpc.InitConfig

/**
 * connection type for android rpc extensions.
 */
enum class EstablishStrategy {
    ContentProvider,
    /**
     * if you choose [EstablishStrategy.Service] as your connection strategy,
     * you must call [InitConfig.Builder.remoteAndroidServiceClass] to let [AIDLConnector] know who the target is.
     */
    Service,
    BroadcastReceiver
}