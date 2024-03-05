package com.dst.rpc.socket

import com.dst.rpc.Connection
import java.net.Socket

/**
 * @author liuzhongao
 * @since 2024/3/5 20:56
 */
internal class SocketConnection(
    private val rpCorrelator: RPCorrelator,
) : Connection {

    private var _socket: Socket? = null

    override val isClosed: Boolean
        get() = this._socket == null || requireNotNull(this._socket).isClosed

    override suspend fun call(
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        TODO("")
    }
}