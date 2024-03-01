package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

data class RemoteInvocationResponse(
    val data: Any?,
    val throwable: Throwable? = null
) : AIDLResponse, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(RemoteInvocationResponse::class.java.classLoader),
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readSerializable(RemoteInvocationResponse::class.java.classLoader, Throwable::class.java)
        } else parcel.readSerializable() as? Throwable
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(this.data)
        parcel.writeSerializable(this.throwable)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID: Long = -7034334841970619516L

        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteInvocationResponse> {
            override fun createFromParcel(parcel: Parcel): RemoteInvocationResponse {
                return RemoteInvocationResponse(parcel)
            }

            override fun newArray(size: Int): Array<RemoteInvocationResponse?> {
                return arrayOfNulls(size)
            }
        }
    }

}