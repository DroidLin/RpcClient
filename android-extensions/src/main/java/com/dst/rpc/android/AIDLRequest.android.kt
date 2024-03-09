package com.dst.rpc.android

import android.os.Parcel
import android.os.Parcelable

internal data class AndroidInvocationRequest(
    val className: String,
    val functionName: String,
    val functionUniqueKey: String,
    val classTypesOfFunctionParameter: List<String>,
    val valuesOfFunctionParameter: List<Any?>,
) : Request, Parcelable {

    constructor(parcel: Parcel) : this(
        className = requireNotNull(parcel.readString()),
        functionName = requireNotNull(parcel.readString()),
        functionUniqueKey = requireNotNull(parcel.readString()),
        classTypesOfFunctionParameter = requireNotNull(parcel.readArrayList(AndroidSuspendInvocationRequest::class.java.classLoader) as? List<String>),
        valuesOfFunctionParameter = requireNotNull(parcel.readArrayList(AndroidSuspendInvocationRequest::class.java.classLoader) as? List<Any?>),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.className)
        parcel.writeString(this.functionName)
        parcel.writeString(this.functionUniqueKey)
        parcel.writeList(this.classTypesOfFunctionParameter)
        parcel.writeList(this.valuesOfFunctionParameter)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID: Long = -3493071081757263356L

        @JvmField
        val CREATOR = object : Parcelable.Creator<AndroidInvocationRequest> {
            override fun createFromParcel(parcel: Parcel): AndroidInvocationRequest {
                return AndroidInvocationRequest(parcel)
            }

            override fun newArray(size: Int): Array<AndroidInvocationRequest?> {
                return arrayOfNulls(size)
            }
        }
    }
}

data class AndroidSuspendInvocationRequest internal constructor(
    val className: String,
    val functionName: String,
    val functionUniqueKey: String,
    val classTypesOfFunctionParameter: List<String>,
    val valuesOfFunctionParameter: List<Any?>,
    internal val rpCallback: RPCallback,
) : Request, Parcelable {

    constructor(parcel: Parcel) : this(
        className = requireNotNull(parcel.readString()),
        functionName = requireNotNull(parcel.readString()),
        functionUniqueKey = requireNotNull(parcel.readString()),
        classTypesOfFunctionParameter = requireNotNull(parcel.readArrayList(AndroidSuspendInvocationRequest::class.java.classLoader) as? List<String>),
        valuesOfFunctionParameter = requireNotNull(parcel.readArrayList(AndroidSuspendInvocationRequest::class.java.classLoader) as? List<Any?>),
        rpCallback = RPCallback(RPCInterface(function = Function.Stub.asInterface(requireNotNull(parcel.readStrongBinder())))),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.className)
        parcel.writeString(this.functionName)
        parcel.writeString(this.functionUniqueKey)
        parcel.writeList(this.classTypesOfFunctionParameter)
        parcel.writeList(this.valuesOfFunctionParameter)
        parcel.writeStrongBinder(this.rpCallback.iBinder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID: Long = -3493071081757263356L

        @JvmField
        val CREATOR = object : Parcelable.Creator<AndroidSuspendInvocationRequest> {
            override fun createFromParcel(parcel: Parcel): AndroidSuspendInvocationRequest {
                return AndroidSuspendInvocationRequest(parcel)
            }

            override fun newArray(size: Int): Array<AndroidSuspendInvocationRequest?> {
                return arrayOfNulls(size)
            }
        }
    }
}

internal data class RPCallbackRequest(val data: Any?, val throwable: Throwable? = null) : Request, Parcelable {

    constructor(parcel: Parcel) : this(
        data = requireNotNull(parcel.readValue(RPCallbackRequest::class.java.classLoader)),
        throwable = requireNotNull(parcel.readSerializable() as? Throwable)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(this.data)
        parcel.writeSerializable(this.throwable)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RPCallbackRequest> {
        private const val serialVersionUID: Long = 5429308687547226465L

        override fun createFromParcel(parcel: Parcel): RPCallbackRequest {
            return RPCallbackRequest(parcel)
        }

        override fun newArray(size: Int): Array<RPCallbackRequest?> {
            return arrayOfNulls(size)
        }
    }

}

internal data class AttachReCorrelatorRequest(
    val rpCorrelator: AndroidRPCorrelator
) : Request, Parcelable {

    constructor(parcel: Parcel) : this(
        rpCorrelator = RPCorrelator(
            rpcInterface = RPCInterface(
                function = Function.Stub.asInterface(
                    requireNotNull(
                        parcel.readStrongBinder()
                    )
                )
            )
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStrongBinder(this.rpCorrelator.iBinder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AttachReCorrelatorRequest> {
        private const val serialVersionUID: Long = -3271507829792609368L

        override fun createFromParcel(parcel: Parcel): AttachReCorrelatorRequest {
            return AttachReCorrelatorRequest(parcel)
        }

        override fun newArray(size: Int): Array<AttachReCorrelatorRequest?> {
            return arrayOfNulls(size)
        }
    }
}