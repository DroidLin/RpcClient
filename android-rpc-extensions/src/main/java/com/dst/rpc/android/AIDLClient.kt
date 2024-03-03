package com.dst.rpc.android

import android.app.Service
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.dst.rpc.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * @author liuzhongao
 * @since 2024/3/1 17:41
 */
internal class AIDLClient(initConfig: InitConfig) : Client {

    private val rootExceptionHandler: ExceptionHandler = initConfig.rootExceptionHandler
    private val connectTimeout: Long = initConfig.connectTimeout
    private val strategy: EstablishStrategy = initConfig.strategy
    private val coroutineContext: CoroutineContext = initConfig.coroutineContext
    private val remoteAndroidServiceClass: Class<out Service>? = initConfig.remoteAndroidServiceClass
    private val androidContext: Context = initConfig.androidContext

    private val coroutineScope = CoroutineScope(this.coroutineContext)
    private val innerRPCorrelatorCache: MutableMap<String, Deferred<RPCorrelator>> = HashMap()

    override suspend fun execute(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        functionOwner: Class<*>,
        functionName: String,
        functionParameterTypes: List<Class<*>>,
        functionParameterValues: List<Any?>,
        isSuspended: Boolean
    ): Any? {
        assertWorkerThread()
        val rpcInterface = this.acquireBinderConnection(sourceAddress = sourceAddress, remoteAddress = remoteAddress)
        return if (isSuspended) {
            rpcInterface.callSuspendFunction(
                functionOwner = functionOwner,
                functionName = functionName,
                argumentTypes = functionParameterTypes.filter { it != Continuation::class.java },
                argumentValue = functionParameterValues
            )
        } else rpcInterface.callFunction(
            functionOwner = functionOwner,
            functionName = functionName,
            argumentTypes = functionParameterTypes,
            argumentValue = functionParameterValues
        )
    }

    /**
     * try to connect to remote process.
     */
    private suspend fun acquireBinderConnection(sourceAddress: RPCAddress, remoteAddress: RPCAddress): RPCorrelator {
        val existingConnectionOperation = this.innerRPCorrelatorCache[remoteAddress.value]
        if (existingConnectionOperation != null) {
            return existingConnectionOperation.await()
        }
        val connectionOperation = this.coroutineScope.async {
            suspendCoroutine { continuation ->
                val timeoutJob = launch {
                    delay(this@AIDLClient.connectTimeout)
                    continuation.resumeWithException(exception = RuntimeException("fail to connection to remote: ${remoteAddress.value} after ${this@AIDLClient.connectTimeout}ms."))
                }
                val localRPCorrelator = RPCorrelator(rpCorrelator = object : RPCorrelator {
                    override fun attachCorrelator(correlator: RPCorrelator) {
                        timeoutJob.cancel()
                        continuation.resume(correlator)
                    }
                })
                val rpContext = RPContext(
                    remoteServiceName = this@AIDLClient.remoteAndroidServiceClass?.name ?: "",
                    sourceAddress = AndroidRPCAddress(Uri.parse(sourceAddress.value)),
                    remoteAddress = AndroidRPCAddress(Uri.parse(remoteAddress.value)),
                    rpCorrelator = localRPCorrelator
                )
                AIDLConnector.attach(strategy = this@AIDLClient.strategy, rpContext = rpContext, androidContext = this@AIDLClient.androidContext)
            }
        }
        this.innerRPCorrelatorCache[remoteAddress.value] = connectionOperation
        return connectionOperation.await()
    }

    /**
     * factory that accept address like
     * ```
     * "android://[hostKey]:[port]"
     * ```
     */
    class AIDLClientFactory : Client.Factory {

        override fun acceptAddress(address: RPCAddress): Boolean {
            return address.scheme == "android"
        }

        override fun newServer(initConfig: InitConfig): Client {
            return AIDLClient(initConfig = initConfig)
        }
    }
}