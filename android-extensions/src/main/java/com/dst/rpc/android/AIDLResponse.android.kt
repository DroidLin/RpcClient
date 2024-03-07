package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

internal data class AndroidParcelableInvocationResponse(
    override val data: Any?,
    override val throwable: Throwable? = null
) : Response, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(AndroidParcelableInvocationResponse::class.java.classLoader),
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readSerializable(AndroidParcelableInvocationResponse::class.java.classLoader, Throwable::class.java)
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
        val CREATOR = object : Parcelable.Creator<AndroidParcelableInvocationResponse> {
            override fun createFromParcel(parcel: Parcel): AndroidParcelableInvocationResponse {
                return AndroidParcelableInvocationResponse(parcel)
            }

            override fun newArray(size: Int): Array<AndroidParcelableInvocationResponse?> {
                return arrayOfNulls(size)
            }
        }
    }
}

internal data class AndroidParcelableInvocationInternalErrorResponse(
    override val throwable: Throwable?
) : Response {
    override val data: Any? get() = null

    companion object {
        private const val serialVersionUID: Long = -6686492997033198982L
    }
}