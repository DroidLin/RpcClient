package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/1 16:23
 */
class Bridge : Parcelable, Serializable {

    @Transient
    private var _next: Bridge? = null

    internal val innerParameterMap: MutableMap<String, Any?> = HashMap()

    private constructor()

    constructor(parcel: Parcel) : this() {
        this.readFromParcel(parcel)
    }

    fun readFromParcel(parcel: Parcel) {
        val parcelableMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readHashMap(Bridge::class.java.classLoader, String::class.java, Any::class.java)
        } else parcel.readHashMap(Bridge::class.java.classLoader) as? MutableMap<String, Any?>
        if (parcelableMap != null) {
            this.innerParameterMap.clear()
            this.innerParameterMap.putAll(parcelableMap)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeMap(this.innerParameterMap)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun recycle() {
        this.innerParameterMap.clear()
        recycle(this)
    }

    companion object {
        private const val serialVersionUID: Long = -90000007L
        private var bridgeParameterHead: Bridge? = null
        @JvmField
        val CREATOR = object : Parcelable.Creator<Bridge> {
            override fun createFromParcel(parcel: Parcel): Bridge {
                return Bridge(parcel)
            }

            override fun newArray(size: Int): Array<Bridge?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun obtain(): Bridge {
            synchronized(this) {
                val head = this.bridgeParameterHead
                if (head != null) {
                    this.bridgeParameterHead = head._next
                    head._next = null
                    return head
                }
            }
            return Bridge()
        }

        @JvmStatic
        private fun recycle(bridgeParameter: Bridge) {
            synchronized(this) {
                bridgeParameter._next = this.bridgeParameterHead
                this.bridgeParameterHead = bridgeParameter
            }
        }
    }
}