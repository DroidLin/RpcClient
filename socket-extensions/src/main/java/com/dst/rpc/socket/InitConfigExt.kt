package com.dst.rpc.socket

import com.dst.rpc.InitConfig
import com.dst.rpc.RPCAddress

/**
 * @author liuzhongao
 * @since 2024/3/7 16:11
 */

private const val KEY_SOURCE_ADDRESS = "key_source_address"

val InitConfig.sourceAddress: RPCAddress
    get() = requireNotNull(this.extraParameters[KEY_SOURCE_ADDRESS] as? RPCAddress)

fun InitConfig.Builder.sourceAddress(sourceAddress: RPCAddress) =
    this.putExtra(KEY_SOURCE_ADDRESS, sourceAddress)