package com.dst.rpc.serializer

import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/5 23:59
 */
interface Serializer {

    fun writeByte(value: Int)

    fun writeByteArray(value: ByteArray, start: Int, end: Int)

    fun writeInt(value: Int)

    fun writeIntArray(value: IntArray, start: Int, end: Int)

    fun writeLong(value: Long)

    fun writeLongArray(value: LongArray, start: Int, end: Int)

    fun writeDouble(value: Double)

    fun writeDoubleArray(value: Double, start: Int, end: Int)

    fun writeChar(value: Char)

    fun writeCharArray(value: CharArray, start: Int, end: Int)

    fun writeFloat(value: Float)

    fun writeFloatArray(value: FloatArray, start: Int, end: Int)

    fun writeShort(value: Short)

    fun writeShortArray(value: ShortArray, start: Int, end: Int)

    fun writeUShort(value: UShort)

    @ExperimentalUnsignedTypes
    fun writeUShortArray(value: UShortArray, start: Int, end: Int)

    fun writeBoolean(value: Boolean)

    fun writeString(value: String)

    fun writeSerializable(serializable: Serializable?)
}