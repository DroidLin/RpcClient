package com.dst.rpc.android

import android.net.Uri
import android.os.Parcelable
import com.dst.rpc.RPCAddress

/**
 * @author liuzhongao
 * @since 2024/3/3 15:06
 */
interface AIDLRPCAddress : RPCAddress, Parcelable {
    val uri: Uri
}