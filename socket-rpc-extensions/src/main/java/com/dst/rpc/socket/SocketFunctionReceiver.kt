package com.dst.rpc.socket

import com.dst.rpc.InitConfig
import com.dst.rpc.OneShotContinuation
import com.dst.rpc.RPCAddress
import com.dst.rpc.socket.serializer.KEY_FUNCTION_SUSPEND_CALLBACK
import com.dst.rpc.socket.serializer.KEY_FUNCTION_TYPE_NON_SUSPEND
import com.dst.rpc.socket.serializer.KEY_FUNCTION_TYPE_SUSPEND
import com.dst.rpc.socket.serializer.SerializeReader
import com.dst.rpc.socket.serializer.SerializeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author liuzhongao
 * @since 2024/3/7 16:10
 */
internal class SocketFunctionReceiver(initConfig: InitConfig) {

    private val sourceAddress = initConfig.sourceAddress
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val rpCorrelator = RPCorrelator()

    private val runnable: (suspend CoroutineScope.() -> Unit) = {
        while (this.isActive) {
            kotlin.runCatching {
                val address = InetSocketAddress(this@SocketFunctionReceiver.sourceAddress.domain, this@SocketFunctionReceiver.sourceAddress.port)
                val socketServer = ServerSocket()
                socketServer.bind(address)
                while (this.isActive) {
                    val clientSocket = socketServer.accept()
                    this@SocketFunctionReceiver.coroutineScope.launch { this@SocketFunctionReceiver.handleRequest(clientSocket) }
                }
            }.onFailure { it.printStackTrace() }
        }
    }

    fun listenToRemoteCall() {
        this.coroutineScope.launch(block = this.runnable)
    }

    private fun handleRequest(clientSocket: Socket) {
        val serializeReader = SerializeReader(clientSocket.getInputStream())
        when (serializeReader.readString()) {
            KEY_FUNCTION_TYPE_NON_SUSPEND -> {
                val functionOwner = requireNotNull(serializeReader.readString()).stringTypeConvert
                val functionName = requireNotNull(serializeReader.readString())
                val argumentTypes = requireNotNull(serializeReader.readList<String>()).stringTypeConvert
                val argumentValue = requireNotNull(serializeReader.readList<Any?>())

                val result: Result<Any?> = kotlin.runCatching {
                    this.rpCorrelator.callFunction(
                        functionOwner = functionOwner,
                        functionName = functionName,
                        argumentTypes = argumentTypes,
                        argumentValue = argumentValue,
                    )
                }
                val byteArray = SerializeWriter().also { serializeWriter ->
                    serializeWriter.writeValue(result.getOrNull())
                    serializeWriter.writeValue(result.exceptionOrNull())
                    serializeWriter.close()
                }.toByteArray()
                clientSocket.getOutputStream().write(byteArray)
                clientSocket.getOutputStream().flush()
                clientSocket.shutdownOutput()
            }
            KEY_FUNCTION_TYPE_SUSPEND -> {
                val functionOwner = requireNotNull(serializeReader.readString()).stringTypeConvert
                val functionName = requireNotNull(serializeReader.readString())
                val argumentType = requireNotNull(serializeReader.readList<String>()).stringTypeConvert
                val argumentValue = requireNotNull(serializeReader.readList<Any?>())
                val socketAddress = requireNotNull(serializeReader.readSerializable() as? RPCAddress)
                val token = serializeReader.readLong()

                val continuation = Continuation<Any?>(EmptyCoroutineContext) { result ->
                    kotlin.runCatching {
                        val tempSocket = Socket().also { socket ->
                            val callbackAddress = InetSocketAddress(socketAddress.domain, socketAddress.port)
                            socket.connect(callbackAddress)
                        }
                        val byteArray = SerializeWriter().also { serializeWriter ->
                            serializeWriter.writeString(KEY_FUNCTION_SUSPEND_CALLBACK)
                            serializeWriter.writeLong(token)
                            serializeWriter.writeValue(result.getOrNull())
                            serializeWriter.writeValue(result.exceptionOrNull())
                            serializeWriter.close()
                        }.toByteArray()
                        tempSocket.getOutputStream().write(byteArray)
                        tempSocket.getOutputStream().flush()
                        tempSocket.getOutputStream().close()
                        tempSocket.shutdownOutput()
                    }
                }
                val oneShotContinuation = OneShotContinuation(continuation)

                val result = kotlin.runCatching {
                    (this.rpCorrelator::callSuspendFunction as Function5<Class<*>, String, List<Class<*>>, List<Any?>, Continuation<Any?>, Any?>)
                        .invoke(functionOwner, functionName, argumentType, argumentValue, oneShotContinuation)
                }
                val byteArray = SerializeWriter().also { serializeWriter ->
                    serializeWriter.writeValue(result.getOrNull())
                    serializeWriter.writeValue(result.exceptionOrNull())
                    serializeWriter.close()
                }.toByteArray()
                clientSocket.getOutputStream().write(byteArray)
                clientSocket.getOutputStream().flush()
                clientSocket.shutdownOutput()
            }
            KEY_FUNCTION_SUSPEND_CALLBACK -> {
                val token = serializeReader.readLong()
                val data = serializeReader.readValue<Any?>()
                val throwable = serializeReader.readValue<Throwable?>()
                SocketAsyncInvocationRegistry.getRPCallback(token)?.callback(data, throwable)
                SocketAsyncInvocationRegistry.removeRPCallback(token)
            }
        }
    }
}