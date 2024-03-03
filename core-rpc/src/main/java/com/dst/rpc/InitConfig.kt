package com.dst.rpc

/**
 * @author liuzhongao
 * @since 2024/3/1 23:06
 */
class InitConfig private constructor(
    /**
     * global rpc exception handler.
     */
    val rootExceptionHandler: ExceptionHandler,
    /**
     * waiting timeout for connect to remote.
     */
    val connectTimeout: Long,
    /**
     * custom parameters for different implementation environment.
     */
    val extraParameters: Map<String, Any>
) {

    class Builder {
        private var exceptionHandler: ExceptionHandler? = null
        private var connectTimeout: Long = 10_000L
        private val extraParameter: MutableMap<String, Any> = HashMap()

        fun rootExceptionHandler(exceptionHandler: ExceptionHandler) =
            apply { this.exceptionHandler = exceptionHandler }

        fun connectTimeout(timeout: Long) = apply { this.connectTimeout = timeout }

        fun putExtra(key: String, value: Any) = apply { this.extraParameter[key] = value }

        fun build(): InitConfig {
            require(this.connectTimeout >= 5_000L) {
                "connectTimeout should not be less than 5s."
            }
            return InitConfig(
                rootExceptionHandler = requireNotNull(this.exceptionHandler),
                connectTimeout = this.connectTimeout,
                extraParameters = this.extraParameter
            )
        }
    }
}