package com.dst.rpc.socket

import com.android.dependencies.serializer.SerializeReader
import com.android.dependencies.serializer.SerializeWriter
import com.dst.rpc.InitConfig
import com.dst.rpc.OneShotContinuation
import com.dst.rpc.Address
import com.dst.rpc.safeUnbox
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

    private val callService = CallService()

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
        if (!acceptAddressInner(this.sourceAddress)) {
            return
        }
        this.coroutineScope.launch(block = this.runnable)
    }

    private fun handleRequest(clientSocket: Socket) {
        val serializeReader = SerializeReader(clientSocket.getInputStream())
        when (serializeReader.readString()) {
            KEY_FUNCTION_TYPE_NON_SUSPEND -> {
                val functionOwner = requireNotNull(serializeReader.readString()).stringTypeConvert
                val functionName = requireNotNull(serializeReader.readString())
                val functionUniqueKey = requireNotNull(serializeReader.readString())
                val argumentTypes = requireNotNull(serializeReader.readList<String>()).stringTypeConvert
                val argumentValue = requireNotNull(serializeReader.readList<Any?>())

                val result: Result<Any?> = kotlin.runCatching {
                    this.callService.callFunction(
                        functionOwner = functionOwner,
                        functionName = functionName,
                        functionUniqueKey = functionUniqueKey,
                        argumentTypes = argumentTypes,
                        argumentValue = argumentValue,
                    )
                }
                val byteArray = SerializeWriter().also { serializeWriter ->
                    serializeWriter.writeValue(result.getOrNull().safeUnbox())
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
                val functionUniqueKey = requireNotNull(serializeReader.readString())
                val argumentType = requireNotNull(serializeReader.readList<String>()).stringTypeConvert
                val argumentValue = requireNotNull(serializeReader.readList<Any?>())
                val socketAddress = requireNotNull(serializeReader.readSerializable() as? Address)
                val token = serializeReader.readLong()

                val socketRPCallback = SocketCallback(socketAddress, token)
                val continuation = Continuation<Any?>(EmptyCoroutineContext) { result ->
                    kotlin.runCatching { socketRPCallback.callback(result.getOrNull().safeUnbox(), result.exceptionOrNull()) }
                }
                val oneShotContinuation = OneShotContinuation(continuation)

                val result = kotlin.runCatching {
                    (this.callService::callSuspendFunction as Function6<Class<*>, String, String, List<Class<*>>, List<Any?>, Continuation<Any?>, Any?>)
                        .invoke(functionOwner, functionName, functionUniqueKey, argumentType, argumentValue, oneShotContinuation)
                }
                val byteArray = SerializeWriter().also { serializeWriter ->
                    serializeWriter.writeValue(result.getOrNull().safeUnbox())
                    serializeWriter.writeValue(result.exceptionOrNull())
                    serializeWriter.close()
                }.toByteArray()
                clientSocket.getOutputStream().write(byteArray)
                clientSocket.getOutputStream().flush()
                clientSocket.shutdownOutput()
            }
            /**
             * receive callback request from remote, usually suspend function changes it running context.
             * like running from Dispatchers.Main to Dispatchers.Default.
             */
            KEY_FUNCTION_SUSPEND_CALLBACK -> {
                val token = serializeReader.readLong()
                val data = serializeReader.readValue<Any?>()
                val throwable = serializeReader.readValue<Throwable?>()
                SocketAsyncCallbackRegistry.getCallback(token)?.callback(data, throwable)
                SocketAsyncCallbackRegistry.removeCallback(token)
            }
        }
    }
}