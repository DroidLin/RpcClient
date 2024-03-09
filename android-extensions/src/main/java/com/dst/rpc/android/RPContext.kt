package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

/**
 * @author liuzhongao
 * @since 2024/3/2 19:22
 */
data class RPContext internal constructor(
    val remoteServiceName: String,
    val sourceAddress: AIDLRPCAddress,
    val remoteAddress: AIDLRPCAddress,
    internal val rpCorrelator: AndroidRPCorrelator
) : Parcelable {

    constructor(parcel: Parcel) : this(
        remoteServiceName = parcel.readString() ?: "",
        sourceAddress = requireNotNull(if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(AIDLRPCAddress::class.java.classLoader, AIDLRPCAddress::class.java)
        } else parcel.readParcelable(AIDLRPCAddress::class.java.classLoader)),
        remoteAddress = requireNotNull(if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(AIDLRPCAddress::class.java.classLoader, AIDLRPCAddress::class.java)
        } else parcel.readParcelable(AIDLRPCAddress::class.java.classLoader)),
        rpCorrelator = RPCorrelator(RPCInterface(function = Function.Stub.asInterface(parcel.readStrongBinder())))
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.remoteServiceName)
        parcel.writeParcelable(this.sourceAddress, 0)
        parcel.writeParcelable(this.remoteAddress, 0)
        parcel.writeStrongBinder(this.rpCorrelator.iBinder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<RPContext> {
            override fun createFromParcel(parcel: Parcel): RPContext {
                return RPContext(parcel)
            }

            override fun newArray(size: Int): Array<RPContext?> {
                return arrayOfNulls(size)
            }
        }
    }
}
