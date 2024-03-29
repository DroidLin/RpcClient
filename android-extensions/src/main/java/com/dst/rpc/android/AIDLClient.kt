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
    private val preAttachCallback: OnPreAttachCallback? = initConfig.preAttachCallback
    private val coroutineScope: CoroutineScope = CoroutineScope(initConfig.coroutineContext)

    override fun openConnection(
        sourceAddress: Address,
        remoteAddress: Address
    ): Connection = this.openConnection(sourceAddress, remoteAddress) { false }

    override fun openConnection(
        sourceAddress: Address,
        remoteAddress: Address,
        exceptionHandler: ExceptionHandler
    ): Connection {
        val combinedExceptionHandler = exceptionHandler + this.rootExceptionHandler
        val rawConnectionProvider = suspend { this.acquireConnection(sourceAddress, remoteAddress) }
        return ExceptionHandleConnection(combinedExceptionHandler, rawConnectionProvider)
    }

    /**
     * try to connect to remote process.
     */
    private suspend fun acquireConnection(sourceAddress: Address, remoteAddress: Address): Connection {
        val existingConnectionOperation = acquireConnectionTask(remoteAddress)
        if (existingConnectionOperation != null) {
            val connection = existingConnectionOperation.await()
            if (!connection.isClosed) {
                return connection
            }
        }
        val connectionOperation = this@AIDLClient.coroutineScope.async {
            suspendCoroutine { continuation ->
                val timeoutJob = launch {
                    delay(this@AIDLClient.connectTimeout)
                    continuation.resumeWithException(exception = RuntimeException("fail to connection to remote: ${remoteAddress.value} after ${this@AIDLClient.connectTimeout}ms."))
                }
                val localCallService = AndroidCallService(callService = object : AndroidCallService {
                    override fun attachCallService(callService: AndroidCallService) {
                        timeoutJob.cancel()
                        continuation.resume(AIDLConnection(callService))
                    }
                })
                val aidlContext = AIDLContext(
                    remoteServiceName = this@AIDLClient.remoteAndroidServiceClass?.name ?: "",
                    sourceAddress = AndroidAddress(sourceAddress),
                    remoteAddress = AndroidAddress(remoteAddress),
                    callService = localCallService
                )
                AIDLConnector.attach(this@AIDLClient.strategy, aidlContext, androidContext)
            }
        }
        acceptConnection(remoteAddress, connectionOperation)
        return connectionOperation.await()
    }

    companion object {
        private val innerConnectionCache: MutableMap<String, Deferred<Connection>> = HashMap()

        internal fun acquireConnectionTask(address: Address): Deferred<Connection>? {
            return synchronized(this.innerConnectionCache) {
                this.innerConnectionCache[address.value]
            }
        }

        internal fun acceptConnection(address: Address, deferred: Deferred<Connection>) {
            synchronized(this.innerConnectionCache) {
                this.innerConnectionCache[address.value] = deferred
            }
        }

        /**
         * establishing a two-way communication through IBinder.
         */
        internal fun twoWayConnectionEstablish(remoteCallService: AndroidCallService) {
            val localCallService = AndroidCallService(object : AndroidCallService {
                override fun attachCallService(callService: AndroidCallService) {}
            })
            remoteCallService.attachCallService(localCallService)
        }
    }
}