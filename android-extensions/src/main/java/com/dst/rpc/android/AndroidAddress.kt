package com.dst.rpc.android

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.dst.rpc.Address

/**
 * @author liuzhongao
 * @since 2024/3/3 14:55
 */
data class AndroidAddress(override val uri: Uri) : AIDLAddress {

    constructor(address: String) : this(Uri.parse(address))

    constructor(address: Address) : this(Uri.parse(address.value))

    constructor(parcel: Parcel) : this(
        uri = requireNotNull(if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(Uri::class.java.classLoader, Uri::class.java)
        } else parcel.readParcelable(Uri::class.java.classLoader))
    )

    override val scheme: String by lazy { this.uri.scheme ?: "" }
    override val domain: String by lazy { this.uri.host ?: "" }
    override val port: Int by lazy { this.uri.port }
    override val value: String by lazy { this.uri.toString() }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID: Long = -2315364102076331037L

        @JvmField
        val CREATOR = object : Parcelable.Creator<AndroidAddress> {
            override fun createFromParcel(parcel: Parcel): AndroidAddress {
                return AndroidAddress(parcel)
            }

            override fun newArray(size: Int): Array<AndroidAddress?> {
                return arrayOfNulls(size)
            }
        }
    }

}