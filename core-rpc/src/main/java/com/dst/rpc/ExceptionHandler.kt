package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/1 00:32
 */
fun interface ExceptionHandler {

    fun handle(throwable: Throwable?): Boolean

    companion object : ExceptionHandler {
        override fun handle(throwable: Throwable?): Boolean = false
    }
}

operator fun ExceptionHandler.plus(handler: ExceptionHandler): ExceptionHandler =
    ExceptionHandler { throwable -> this.handle(throwable = throwable) || handler.handle(throwable = throwable) }