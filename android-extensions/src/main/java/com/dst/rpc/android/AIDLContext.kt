package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

/**
 * @author liuzhongao
 * @since 2024/3/2 19:22
 */
data class AIDLContext internal constructor(
    val remoteServiceName: String,
    val sourceAddress: AIDLAddress,
    val remoteAddress: AIDLAddress,
    internal val callService: AndroidCallService
) : Parcelable {

    constructor(parcel: Parcel) : this(
        remoteServiceName = parcel.readString() ?: "",
        sourceAddress = requireNotNull(if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(AIDLAddress::class.java.classLoader, AIDLAddress::class.java)
        } else parcel.readParcelable(AIDLAddress::class.java.classLoader)),
        remoteAddress = requireNotNull(if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(AIDLAddress::class.java.classLoader, AIDLAddress::class.java)
        } else parcel.readParcelable(AIDLAddress::class.java.classLoader)),
        callService = AndroidCallService(AIDLFunction(function = Function.Stub.asInterface(parcel.readStrongBinder())))
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.remoteServiceName)
        parcel.writeParcelable(this.sourceAddress, 0)
        parcel.writeParcelable(this.remoteAddress, 0)
        parcel.writeStrongBinder(this.callService.iBinder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<AIDLContext> {
            override fun createFromParcel(parcel: Parcel): AIDLContext {
                return AIDLContext(parcel)
            }

            override fun newArray(size: Int): Array<AIDLContext?> {
                return arrayOfNulls(size)
            }
        }
    }
}
