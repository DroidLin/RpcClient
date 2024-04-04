package com.dst.rpc.android.component

import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.dst.rpc.android.AIDLContext

/**
 * @author liuzhongao
 * @since 2024/3/2 19:21
 */

internal const val KEY_PROCESS_REQUEST_BUNDLE = "key_rpc_connection_context"

internal var Intent.rpcContext: AIDLContext?
    get() = IntentCompat.getParcelableExtra(this, KEY_PROCESS_REQUEST_BUNDLE, AIDLContext::class.java)
    set(value) {
        this.putExtra(KEY_PROCESS_REQUEST_BUNDLE, value)
    }

internal var Bundle.rpcContext: AIDLContext?
    set(value) {
        this.putParcelable(KEY_PROCESS_REQUEST_BUNDLE, value)
    }
    get() = kotlin.runCatching {
        BundleCompat.getParcelable(this, KEY_PROCESS_REQUEST_BUNDLE, AIDLContext::class.java)
    }.onFailure { it.printStackTrace() }.getOrNull()