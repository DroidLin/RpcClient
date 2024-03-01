package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

internal data class RemoteInvocationRequest(
    val className: String,
    val functionName: String,
    val classTypesOfFunctionParameter: List<String>,
    val valuesOfFunctionParameter: List<Any?>,
    val isSuspendFunction: Boolean,
) : AIDLRequest, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        ArrayList<String>().also { arrayList ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readList(arrayList, RemoteInvocationRequest::class.java.classLoader, String::class.java)
            } else parcel.readList(arrayList, RemoteInvocationRequest::class.java.classLoader)
        },
        ArrayList<Any?>().also { arrayList ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readList(arrayList, RemoteInvocationRequest::class.java.classLoader, Any::class.java)
            } else parcel.readList(arrayList, RemoteInvocationRequest::class.java.classLoader)
        },
        parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.className)
        parcel.writeString(this.functionName)
        parcel.writeList(this.classTypesOfFunctionParameter)
        parcel.writeList(this.valuesOfFunctionParameter)
        parcel.writeInt(if (this.isSuspendFunction) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID: Long = -3493071081757263356L

        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteInvocationRequest> {
            override fun createFromParcel(parcel: Parcel): RemoteInvocationRequest {
                return RemoteInvocationRequest(parcel)
            }

            override fun newArray(size: Int): Array<RemoteInvocationRequest?> {
                return arrayOfNulls(size)
            }
        }
    }
}