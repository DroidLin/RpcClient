package com.dst.rpc.android

import android.app.Service
import android.content.Context
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
    private val remoteAndroidServiceClass: Class<out Service>? = initConfig.remoteAndroidServiceClass
    private val androidContext: Context = initConfig.androidContext

    override fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress
    ): Connection = this.openConnection(sourceAddress, remoteAddress) { false }

    override fun openConnection(
        sourceAddress: RPCAddress,
        remoteAddress: RPCAddress,
        exceptionHandler: ExceptionHandler
    ): Connection {
        val combinedExceptionHandler = exceptionHandler + this.rootExceptionHandler
        val rawConnectionProvider = suspend { this.acquireRPCorrelator(sourceAddress, remoteAddress) }
        return ExceptionHandleConnection(combinedExceptionHandler, rawConnectionProvider)
    }

    /**
     * try to connect to remote process.
     */
    private suspend fun acquireRPCorrelator(sourceAddress: RPCAddress, remoteAddress: RPCAddress): Connection {
        val existingConnectionOperation = acquireConnectionTask(remoteAddress)
        if (existingConnectionOperation != null) {
            val connection = existingConnectionOperation.await()
            if (!connection.isClosed) {
                return connection
            }
        }
        val connectionOperation = coroutineScope {
            async {
                suspendCoroutine { continuation ->
                    val timeoutJob = launch {
                        delay(this@AIDLClient.connectTimeout)
                        continuation.resumeWithException(exception = RuntimeException("fail to connection to remote: ${remoteAddress.value} after ${this@AIDLClient.connectTimeout}ms."))
                    }
                    val localRPCorrelator = RPCorrelator(rpCorrelator = object : AndroidRPCorrelator {
                        override fun attachCorrelator(correlator: AndroidRPCorrelator) {
                            timeoutJob.cancel()
                            continuation.resume(AIDLConnection(correlator))
                        }
                    })
                    val rpContext = RPContext(
                        remoteServiceName = this@AIDLClient.remoteAndroidServiceClass?.name ?: "",
                        sourceAddress = AndroidRPCAddress(sourceAddress),
                        remoteAddress = AndroidRPCAddress(remoteAddress),
                        rpCorrelator = localRPCorrelator
                    )
                    AIDLConnector.attach(strategy = this@AIDLClient.strategy, rpContext = rpContext, androidContext = this@AIDLClient.androidContext)
                }
            }
        }
        acceptConnectionTask(remoteAddress, connectionOperation)
        return connectionOperation.await()
    }

    companion object {
        private val innerConnectionCache: MutableMap<String, Deferred<Connection>> = HashMap()

        internal fun acquireConnectionTask(rpcAddress: RPCAddress): Deferred<Connection>? {
            return synchronized(this.innerConnectionCache) {
                this.innerConnectionCache[rpcAddress.value]
            }
        }

        internal fun acceptConnectionTask(rpcAddress: RPCAddress, deferred: Deferred<Connection>) {
            synchronized(this.innerConnectionCache) {
                this.innerConnectionCache[rpcAddress.value] = deferred
            }
        }

        internal fun twoWayConnectionEstablish(remoteRPCorrelator: AndroidRPCorrelator) {
            val localRPCorrelator = object : AndroidRPCorrelator {
                override fun attachCorrelator(correlator: AndroidRPCorrelator) {}
            }
            remoteRPCorrelator.attachCorrelator(localRPCorrelator)
        }
    }
}