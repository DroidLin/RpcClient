package com.dst.rpc.android

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/1 16:23
 */
internal class TransportBridge : Parcelable, Serializable {

    @Transient
    private var _next: TransportBridge? = null

    internal val innerParameterMap: MutableMap<String, Any?> = HashMap()

    private constructor()

    constructor(parcel: Parcel) : this() {
        this.readFromParcel(parcel)
    }

    fun readFromParcel(parcel: Parcel) {
        val parcelableMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readHashMap(TransportBridge::class.java.classLoader, String::class.java, Any::class.java)
        } else parcel.readHashMap(TransportBridge::class.java.classLoader) as? MutableMap<String, Any?>
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

    fun release() {
        this.innerParameterMap.clear()
    }

    companion object {
        private const val serialVersionUID: Long = -90000007L
        private var bridgeParameterHead: TransportBridge? = null
        @JvmField
        val CREATOR = object : Parcelable.Creator<TransportBridge> {
            override fun createFromParcel(parcel: Parcel): TransportBridge {
                return TransportBridge(parcel)
            }

            override fun newArray(size: Int): Array<TransportBridge?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun obtain(): TransportBridge {
            synchronized(TransportBridge::class.java) {
                val head = this.bridgeParameterHead
                if (head != null) {
                    this.bridgeParameterHead = head._next
                    head._next = null
                    return head
                }
            }
            return TransportBridge()
        }

        @JvmStatic
        fun recycle(transportBridge: TransportBridge) {
            synchronized(this) {
                transportBridge.release()
                transportBridge._next = this.bridgeParameterHead
                this.bridgeParameterHead = transportBridge
            }
        }
    }
}