package com.dst.rpc.android

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.dst.rpc.RPCAddress

/**
 * @author liuzhongao
 * @since 2024/3/3 15:06
 */
interface AIDLRPCAddress : RPCAddress, Parcelable {
    val uri: Uri
}

internal fun AIDLRPCAddress(): AIDLRPCAddress = AIDLRPCAddressImpl

private object AIDLRPCAddressImpl : AIDLRPCAddress {

    private fun readResolve(): Any = AIDLRPCAddressImpl

    private const val serialVersionUID: Long = -7365291567825755168L

    override val uri: Uri get() = Uri.EMPTY
    override val scheme: String get() = ""
    override val domain: String get() = ""
    override val port: Int get() = -1

    override fun writeToParcel(parcel: Parcel, flags: Int) {}
    override fun describeContents(): Int = 0

    override fun toString(): String = ""

    @JvmField
    val CREATOR =  object : Parcelable.Creator<AIDLRPCAddressImpl> {
        override fun createFromParcel(parcel: Parcel): AIDLRPCAddressImpl {
            return AIDLRPCAddressImpl
        }

        override fun newArray(size: Int): Array<AIDLRPCAddressImpl?> {
            return arrayOfNulls(size)
        }
    }
}