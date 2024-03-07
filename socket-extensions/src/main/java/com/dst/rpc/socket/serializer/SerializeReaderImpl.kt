package com.dst.rpc.socket.serializer

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/6 15:57
 */
fun SerializeReader(byteArray: ByteArray): SerializeReader = SerializeReaderImpl(byteArray)

fun SerializeReader(inputStream: InputStream): SerializeReader = SerializeReaderImpl(inputStream)

private class SerializeReaderImpl : SerializeReader {

    private val _objectInputStream: ObjectInputStream

    constructor(byteArray: ByteArray) {
        this._objectInputStream = ObjectInputStream(ByteArrayInputStream(byteArray))
    }

    constructor(inputStream: InputStream) {
        this._objectInputStream = ObjectInputStream(inputStream)
    }

    override fun <T> readValue(): T? {
        val valueType = this._objectInputStream.readInt()
        if (valueType.isArrayOrListType) {
            val arrayOrListCount = this._objectInputStream.readInt()
            return when (valueType) {
                TYPE_BYTE_ARRAY -> ByteArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readByte()
                    }
                }
                TYPE_INT_ARRAY -> IntArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readInt()
                    }
                }
                TYPE_LONG_ARRAY -> LongArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readLong()
                    }
                }
                TYPE_DOUBLE_ARRAY -> DoubleArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readDouble()
                    }
                }
                TYPE_FLOAT_ARRAY -> FloatArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readFloat()
                    }
                }
                TYPE_CHAR_ARRAY -> CharArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readChar()
                    }
                }
                TYPE_SHORT_ARRAY -> ShortArray(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this._objectInputStream.readShort()
                    }
                }
                TYPE_LIST -> ArrayList<Any?>(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array += this.readValue()
                    }
                }
                TYPE_ARRAY -> arrayOfNulls<Any?>(arrayOrListCount).also { array ->
                    for (index in 0 until arrayOrListCount) {
                        array[index] = this.readValue()
                    }
                }
                else -> throw UnsupportedValueTypeException("unsupported value type: $valueType")
            } as? T
        }
        return when (valueType) {
            TYPE_NULL -> null
            TYPE_BYTE -> this._objectInputStream.readByte()
            TYPE_INT -> this._objectInputStream.readInt()
            TYPE_LONG -> this._objectInputStream.readLong()
            TYPE_FLOAT -> this._objectInputStream.readFloat()
            TYPE_DOUBLE -> this._objectInputStream.readDouble()
            TYPE_CHAR -> this._objectInputStream.readChar()
            TYPE_SHORT -> this._objectInputStream.readShort()
            TYPE_BOOLEAN -> this._objectInputStream.readBoolean()
            TYPE_STRING -> this._objectInputStream.readUTF()
            TYPE_SERIALIZABLE -> this._objectInputStream.readObject()
            else -> throw UnsupportedValueTypeException("unsupported value type: $valueType")
        } as? T
    }

    override fun readByte(): Byte = this.readValue() ?: 0
    override fun readInt(): Int = this.readValue() ?: 0
    override fun readLong(): Long = this.readValue() ?: 0
    override fun readDouble(): Double = this.readValue() ?: 0.0
    override fun readChar(): Char = this.readValue() ?: Char.MIN_VALUE
    override fun readFloat(): Float = this.readValue() ?: 0f
    override fun readShort(): Short = this.readValue() ?: 0
    override fun readBoolean(): Boolean = this.readValue() ?: false
    override fun readString(): String? = this.readValue()
    override fun readSerializable(): Serializable? = this.readValue() as? Serializable
    override fun readByteArray(): ByteArray? = this.readValue() as? ByteArray
    override fun readIntArray(): IntArray? = this.readValue() as? IntArray
    override fun readLongArray(): LongArray? = this.readValue() as? LongArray
    override fun readDoubleArray(): DoubleArray? = this.readValue() as? DoubleArray
    override fun readCharArray(): CharArray? = this.readValue() as? CharArray
    override fun readFloatArray(): FloatArray? = this.readValue() as? FloatArray
    override fun readShortArray(): ShortArray? = this.readValue() as? ShortArray
    override fun <T> readArray(): Array<T>? = this.readValue()
    override fun <T> readList(): List<T>? = this.readValue()
    override fun close(): Unit = this._objectInputStream.close()
}