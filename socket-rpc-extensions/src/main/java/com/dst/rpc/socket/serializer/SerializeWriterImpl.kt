package com.dst.rpc.socket.serializer

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * @author liuzhongao
 * @since 2024/3/6 15:58
 */
fun SerializeWriter(): SerializeWriter = SerializeWriterImpl()

internal class SerializeWriterImpl : SerializeWriter {

    private val _byteArrayOutputStream = ByteArrayOutputStream()
    private val _objectOutputStream = ObjectOutputStream(this._byteArrayOutputStream)

    override fun writeValue(value: Any?) {
        when (val valueType = this.getValueType(value = value)) {
            TYPE_NULL -> this.writeNull()
            TYPE_BYTE -> this.writeByte(value as Byte)
            TYPE_INT -> this.writeInt(value as Int)
            TYPE_LONG -> this.writeLong(value as Long)
            TYPE_FLOAT -> this.writeFloat(value as Float)
            TYPE_DOUBLE -> this.writeDouble(value as Double)
            TYPE_CHAR -> this.writeChar(value as Char)
            TYPE_SHORT -> this.writeShort(value as Short)
            TYPE_BOOLEAN -> this.writeBoolean(value as Boolean)
            TYPE_STRING -> this.writeString(value as? String)
            TYPE_SERIALIZABLE -> this.writeSerializable(value as? Serializable)
            TYPE_BYTE_ARRAY -> this.writeByteArray(value as? ByteArray)
            TYPE_INT_ARRAY -> this.writeIntArray(value as? IntArray)
            TYPE_LONG_ARRAY -> this.writeLongArray(value as? LongArray)
            TYPE_DOUBLE_ARRAY -> this.writeDoubleArray(value as? DoubleArray)
            TYPE_FLOAT_ARRAY -> this.writeFloatArray(value as? FloatArray)
            TYPE_CHAR_ARRAY -> this.writeCharArray(value as? CharArray)
            TYPE_SHORT_ARRAY -> this.writeShortArray(value as? ShortArray)
            TYPE_LIST -> this.writeList(value as? List<*>)
            else -> throw UnsupportedValueTypeException("unsupported value type: $valueType")
        }
    }

    private fun getValueType(value: Any?): Int {
        if (value == null) {
            return TYPE_NULL
        }
        return when (value) {
            is Byte -> TYPE_BYTE
            is Int -> TYPE_INT
            is Long -> TYPE_LONG
            is Double -> TYPE_DOUBLE
            is Float -> TYPE_FLOAT
            is Short -> TYPE_SHORT
            is Char -> TYPE_CHAR
            is Boolean -> TYPE_BOOLEAN
            is ByteArray -> TYPE_BYTE_ARRAY
            is IntArray -> TYPE_INT_ARRAY
            is LongArray -> TYPE_LONG_ARRAY
            is DoubleArray -> TYPE_DOUBLE_ARRAY
            is FloatArray -> TYPE_FLOAT_ARRAY
            is ShortArray -> TYPE_SHORT_ARRAY
            is CharArray -> TYPE_CHAR_ARRAY
            is List<*> -> TYPE_LIST
            is String -> TYPE_STRING
            is Serializable -> TYPE_SERIALIZABLE
            else -> throw UnsupportedValueTypeException("unsupported value type: ${value.javaClass.name}")
        }
    }

    private fun writeNull() {
        this._objectOutputStream.writeInt(TYPE_NULL)
    }

    override fun writeByte(value: Byte) {
        this._objectOutputStream.writeInt(TYPE_BYTE)
        this._objectOutputStream.writeByte(value.toInt())
    }

    override fun writeInt(value: Int) {
        this._objectOutputStream.writeInt(TYPE_INT)
        this._objectOutputStream.writeInt(value)
    }

    override fun writeLong(value: Long) {
        this._objectOutputStream.writeInt(TYPE_LONG)
        this._objectOutputStream.writeLong(value)
    }

    override fun writeDouble(value: Double) {
        this._objectOutputStream.writeInt(TYPE_DOUBLE)
        this._objectOutputStream.writeDouble(value)
    }

    override fun writeChar(value: Char) {
        this._objectOutputStream.writeInt(TYPE_CHAR)
        this._objectOutputStream.writeChar(value.code)
    }

    override fun writeFloat(value: Float) {
        this._objectOutputStream.writeInt(TYPE_FLOAT)
        this._objectOutputStream.writeFloat(value)
    }

    override fun writeShort(value: Short) {
        this._objectOutputStream.writeInt(TYPE_SHORT)
        this._objectOutputStream.writeShort(value.toInt())
    }

    override fun writeBoolean(value: Boolean) {
        this._objectOutputStream.writeInt(TYPE_BOOLEAN)
        this._objectOutputStream.writeBoolean(value)
    }

    override fun writeString(value: String?) {
        this._objectOutputStream.writeInt(TYPE_STRING)
        this._objectOutputStream.writeChars(value ?: "")
    }

    override fun writeSerializable(serializable: Serializable?) {
        this._objectOutputStream.writeInt(TYPE_SERIALIZABLE)
        this._objectOutputStream.writeObject(serializable)
    }

    override fun writeByteArray(value: ByteArray?) {
        if (value == null) {
            this.writeByteArray(null, 0, 0)
        } else this.writeByteArray(value, 0, value.size - 1)
    }

    override fun writeByteArray(value: ByteArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_BYTE_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_BYTE_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        this._objectOutputStream.write(value, start, end)
    }

    override fun writeIntArray(value: IntArray?) {
        if (value == null) {
            this.writeIntArray(null, 0, 0)
        } else this.writeIntArray(value, 0, value.size - 1)
    }

    override fun writeIntArray(value: IntArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_INT_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_INT_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeInt(value[index])
        }
    }

    override fun writeLongArray(value: LongArray?) {
        if (value == null) {
            this.writeLongArray(null, 0, 0)
        } else this.writeLongArray(value, 0, value.size - 1)
    }

    override fun writeLongArray(value: LongArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_LONG_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_LONG_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeLong(value[index])
        }
    }

    override fun writeDoubleArray(value: DoubleArray?) {
        if (value == null) {
            this.writeDoubleArray(null, 0, 0)
        } else this.writeDoubleArray(value, 0, value.size - 1)
    }

    override fun writeDoubleArray(value: DoubleArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_DOUBLE_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_DOUBLE_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeDouble(value[index])
        }
    }

    override fun writeCharArray(value: CharArray?) {
        if (value == null) {
            this.writeCharArray(null, 0, 0)
        } else this.writeCharArray(value, 0, value.size - 1)
    }

    override fun writeCharArray(value: CharArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_CHAR_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_CHAR_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeChar(value[index].code)
        }
    }

    override fun writeFloatArray(value: FloatArray?) {
        if (value == null) {
            this.writeFloatArray(null, 0, 0)
        } else this.writeFloatArray(value, 0, value.size - 1)
    }

    override fun writeFloatArray(value: FloatArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_FLOAT_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_FLOAT_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeFloat(value[index])
        }
    }

    override fun writeShortArray(value: ShortArray?) {
        if (value == null) {
            this.writeShortArray(null, 0, 0)
        } else this.writeShortArray(value, 0, value.size - 1)
    }

    override fun writeShortArray(value: ShortArray?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_SHORT_ARRAY)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_SHORT_ARRAY)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this._objectOutputStream.writeShort(value[index].toInt())
        }
    }

    override fun writeList(value: List<Any?>?) {
        if (value == null) {
            this.writeList(null, 0, 0)
        } else this.writeList(value, 0, value.size - 1)
    }

    override fun writeList(value: List<Any?>?, start: Int, end: Int) {
        if (value == null) {
            this._objectOutputStream.writeInt(TYPE_LIST)
            this._objectOutputStream.writeInt(0)
            return
        }
        this._objectOutputStream.writeInt(TYPE_LIST)
        this._objectOutputStream.writeInt(end - start)
        for (index in start until end) {
            this.writeValue(value[index])
        }
    }

    override fun toByteArray(): ByteArray = this._byteArrayOutputStream.toByteArray()
}