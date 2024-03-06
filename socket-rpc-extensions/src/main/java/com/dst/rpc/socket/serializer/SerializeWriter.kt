package com.dst.rpc.socket.serializer

import java.io.Closeable
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/6 15:48
 */
interface SerializeWriter : Closeable {
    fun writeValue(value: Any?)
    fun writeByte(value: Byte)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeDouble(value: Double)
    fun writeChar(value: Char)
    fun writeFloat(value: Float)
    fun writeShort(value: Short)
    fun writeBoolean(value: Boolean)
    fun writeString(value: String?)
    fun writeSerializable(serializable: Serializable?)
    fun writeByteArray(value: ByteArray?)
    fun writeByteArray(value: ByteArray?, start: Int, end: Int)
    fun writeIntArray(value: IntArray?)
    fun writeIntArray(value: IntArray?, start: Int, end: Int)
    fun writeLongArray(value: LongArray?)
    fun writeLongArray(value: LongArray?, start: Int, end: Int)
    fun writeDoubleArray(value: DoubleArray?)
    fun writeDoubleArray(value: DoubleArray?, start: Int, end: Int)
    fun writeCharArray(value: CharArray?)
    fun writeCharArray(value: CharArray?, start: Int, end: Int)
    fun writeFloatArray(value: FloatArray?)
    fun writeFloatArray(value: FloatArray?, start: Int, end: Int)
    fun writeShortArray(value: ShortArray?)
    fun writeShortArray(value: ShortArray?, start: Int, end: Int)
    fun <T> writeArray(value: Array<T>?)
    fun <T> writeArray(value: Array<T>?, start: Int, end: Int)
    fun <T> writeList(value: List<T>?)
    fun <T> writeList(value: List<T>?, start: Int, end: Int)

    fun toByteArray(): ByteArray
}