package com.dst.rpc.socket

import com.dst.rpc.InitConfig
import com.dst.rpc.Address

/**
 * @author liuzhongao
 * @since 2024/3/7 16:11
 */

private const val KEY_SOURCE_ADDRESS = "key_source_address"

val InitConfig.sourceAddress: Address
    get() = requireNotNull(this.extraParameters[KEY_SOURCE_ADDRESS] as? Address)

fun InitConfig.Builder.sourceAddress(sourceAddress: Address) =
    this.putExtra(KEY_SOURCE_ADDRESS, sourceAddress)