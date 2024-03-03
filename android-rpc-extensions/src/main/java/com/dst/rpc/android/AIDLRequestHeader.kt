package com.dst.rpc.android

import android.os.Parcel
import android.os.Parcelable
import com.dst.rpc.Header
import com.dst.rpc.RPCAddress

interface AIDLRequestHeader : Header, Parcelable {
    override val sourceAddress: AIDLRPCAddress
    override val remoteAddress: AIDLRPCAddress
}

internal fun AIDLRequestHeader(): AIDLRequestHeader = EmptyAIDLRequestHeader

private object EmptyAIDLRequestHeader : AIDLRequestHeader {

    private const val serialVersionUID: Long = 4649879274065729421L

    override val sourceAddress: AIDLRPCAddress = AIDLRPCAddress()
    override val remoteAddress: AIDLRPCAddress = AIDLRPCAddress()

    private fun readResolve(): Any = EmptyAIDLRequestHeader

    override fun writeToParcel(parcel: Parcel, flags: Int) {}

    override fun describeContents(): Int {
        return 0
    }

    @JvmField
    val CREATOR = object : Parcelable.Creator<EmptyAIDLRequestHeader> {

        override fun createFromParcel(parcel: Parcel): EmptyAIDLRequestHeader {
            return EmptyAIDLRequestHeader
        }

        override fun newArray(size: Int): Array<EmptyAIDLRequestHeader?> {
            return arrayOfNulls(size)
        }
    }
}