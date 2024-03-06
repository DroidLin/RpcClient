package com.dst.rpc.socket.serializer

import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/6 15:57
 */
fun SerializeReader(byteArray: ByteArray): SerializeReader = SerializeReaderImpl(byteArray)

internal class SerializeReaderImpl(byteArray: ByteArray) : SerializeReader {

    private val _objectInputStream = ObjectInputStream(ByteArrayInputStream(byteArray))

    override fun readValue(): Any? {
        val valueType = this._objectInputStream.readInt()
        if (valueType.isArrayOrListType) {
            val arrayOrListCount = this._objectInputStream.readInt()
            return when (valueType) {
                TYPE_BYTE_ARRAY -> {
                    val byteArray = ByteArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        byteArray[index] = this._objectInputStream.readByte()
                    }
                }
                TYPE_INT_ARRAY -> {
                    val intArray = IntArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        intArray[index] = this._objectInputStream.readInt()
                    }
                }
                TYPE_LONG_ARRAY -> {
                    val longArray = LongArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        longArray[index] = this._objectInputStream.readLong()
                    }
                }
                TYPE_DOUBLE_ARRAY -> {
                    val doubleArray = DoubleArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        doubleArray[index] = this._objectInputStream.readDouble()
                    }
                }
                TYPE_FLOAT_ARRAY -> {
                    val floatArray = FloatArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        floatArray[index] = this._objectInputStream.readFloat()
                    }
                }
                TYPE_CHAR_ARRAY -> {
                    val charArray = CharArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        charArray[index] = this._objectInputStream.readChar()
                    }
                }
                TYPE_SHORT_ARRAY -> {
                    val shortArray = ShortArray(arrayOrListCount)
                    for (index in 0 until arrayOrListCount) {
                        shortArray[index] = this._objectInputStream.readShort()
                    }
                }
                TYPE_LIST -> {
                    val list = ArrayList<Any?>()
                    for (index in 0 until arrayOrListCount) {
                        list += this.readValue()
                    }
                }
                else -> throw UnsupportedValueTypeException("unsupported value type: $valueType")
            }
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
        }
    }

    override fun readByte(): Byte {
        return this.readValue() as Byte
    }

    override fun readInt(): Int {
        return this.readValue() as Int
    }

    override fun readLong(): Long {
        return this.readValue() as Long
    }

    override fun readDouble(): Double {
        return this.readValue() as Double
    }

    override fun readChar(): Char {
        return this.readValue() as Char
    }

    override fun readFloat(): Float {
        return this.readValue() as Float
    }

    override fun readShort(): Short {
        return this.readValue() as Short
    }

    override fun readBoolean(): Boolean {
        return this.readValue() as Boolean
    }

    override fun readString(): String? {
        return this.readValue() as? String
    }

    override fun readSerializable(): Serializable? {
        return this.readValue() as? Serializable
    }

    override fun readByteArray(): ByteArray? {
        return this.readValue() as? ByteArray
    }

    override fun readIntArray(): IntArray? {
        return this.readValue() as? IntArray
    }

    override fun readLongArray(): LongArray? {
        return this.readValue() as? LongArray
    }

    override fun readDoubleArray(): DoubleArray? {
        return this.readValue() as? DoubleArray
    }

    override fun readCharArray(): CharArray? {
        return this.readValue() as? CharArray
    }

    override fun readFloatArray(): FloatArray? {
        return this.readValue() as? FloatArray
    }

    override fun readShortArray(): ShortArray? {
        return this.readValue() as? ShortArray
    }

    override fun readList(): List<Any?>? {
        return this.readValue() as? List<Any?>
    }
}