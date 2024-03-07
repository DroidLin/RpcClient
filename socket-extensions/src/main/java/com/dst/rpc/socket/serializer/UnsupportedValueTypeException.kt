package com.dst.rpc.socket.serializer

/**
 * @author liuzhongao
 * @since 2024/3/6 15:11
 */
class UnsupportedValueTypeException : RuntimeException {

    constructor() : super()
    constructor(p0: String?) : super(p0)
    constructor(p0: String?, p1: Throwable?) : super(p0, p1)
    constructor(p0: Throwable?) : super(p0)
    constructor(p0: String?, p1: Throwable?, p2: Boolean, p3: Boolean) : super(p0, p1, p2, p3)

    companion object {
        private const val serialVersionUID: Long = -5336468405159626291L
    }
}