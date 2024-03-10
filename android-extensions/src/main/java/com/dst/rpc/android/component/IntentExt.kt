package com.dst.rpc.android.component

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.dst.rpc.android.AIDLContext

/**
 * @author liuzhongao
 * @since 2024/3/2 19:21
 */

internal const val KEY_PROCESS_REQUEST_BUNDLE = "key_rpc_connection_context"

internal var Intent.rpcContext: AIDLContext?
    get() = this.extras?.rpcContext
    set(value) {
        this.putExtra(KEY_PROCESS_REQUEST_BUNDLE, value)
    }

internal var Bundle.rpcContext: AIDLContext?
    set(value) {
        this.putParcelable(KEY_PROCESS_REQUEST_BUNDLE, value)
    }
    get() = kotlin.runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getParcelable(KEY_PROCESS_REQUEST_BUNDLE, AIDLContext::class.java)
        } else this.getParcelable(KEY_PROCESS_REQUEST_BUNDLE)
    }.onFailure { it.printStackTrace() }.getOrNull()