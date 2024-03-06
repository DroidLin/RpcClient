package com.dst.rpc.socket.serializer

/**
 * @author liuzhongao
 * @since 2024/3/6 16:20
 */

internal const val TYPE_NULL = -1
internal const val TYPE_BYTE = 0
internal const val TYPE_INT = 1
internal const val TYPE_LONG = 2
internal const val TYPE_FLOAT = 3
internal const val TYPE_DOUBLE = 4
internal const val TYPE_CHAR = 5
internal const val TYPE_SHORT = 6
internal const val TYPE_BOOLEAN = 7
internal const val TYPE_STRING = 8
internal const val TYPE_SERIALIZABLE = 9

internal const val TYPE_BYTE_ARRAY = 10
internal const val TYPE_INT_ARRAY = 11
internal const val TYPE_LONG_ARRAY = 12
internal const val TYPE_DOUBLE_ARRAY = 13
internal const val TYPE_FLOAT_ARRAY = 14
internal const val TYPE_CHAR_ARRAY = 15
internal const val TYPE_SHORT_ARRAY = 16

internal const val TYPE_LIST = 17
internal const val TYPE_ARRAY = 18

internal val Int.isArrayOrListType: Boolean
    get() = this == TYPE_BYTE_ARRAY || this == TYPE_INT_ARRAY || this == TYPE_LONG_ARRAY ||
            this == TYPE_DOUBLE_ARRAY || this == TYPE_FLOAT_ARRAY || this == TYPE_CHAR_ARRAY ||
            this == TYPE_SHORT_ARRAY || this == TYPE_LIST || this == TYPE_ARRAY