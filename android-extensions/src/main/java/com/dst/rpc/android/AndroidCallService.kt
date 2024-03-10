package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.CallService

/**
 * @author liuzhongao
 * @since 2024/3/3 16:22
 */
interface AndroidCallService : CallService {

    fun attachCallService(callService: AndroidCallService)
}

internal val AndroidCallService.iBinder: IBinder
    get() = when (this) {
        is AndroidCallServiceProxy -> this.aidlFunction.iBinder
        is AndroidCallServiceStub -> this.aidlFunction.iBinder
        else -> throw IllegalArgumentException("unknown type of current AndroidCallService: ${this.javaClass.name}")
    }
