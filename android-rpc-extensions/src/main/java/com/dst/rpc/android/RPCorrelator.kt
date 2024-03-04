package com.dst.rpc.android

import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author liuzhongao
 * @since 2024/3/3 16:22
 */
internal interface RPCorrelator {

    val isOpen: Boolean get() = false

    fun attachCorrelator(correlator: RPCorrelator)

    fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = null

    suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = null
}

internal val RPCorrelator.iBinder: IBinder
    get() = when (this) {
        is RPCorrelatorProxy -> this.rpcInterface.iBinder
        is RPCorrelatorStub -> this.rpcInterface.iBinder
        else -> throw IllegalArgumentException("unknown type of current RPCorrelator: ${this.javaClass.name}")
    }

internal fun RPCorrelator(rpCorrelator: RPCorrelator): RPCorrelator = RPCorrelatorStub(rpCorrelator)

internal fun RPCorrelator(rpcInterface: RPCInterface): RPCorrelator = RPCorrelatorProxy(rpcInterface)

private class RPCorrelatorProxy(val rpcInterface: RPCInterface) : RPCorrelator {
    override val isOpen: Boolean get() = rpcInterface.isAlive

    override fun attachCorrelator(correlator: RPCorrelator) {
        val request = AttachReCorrelatorRequest(correlator)
        this.rpcInterface.invoke(request = request)
    }

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val request = AndroidInvocationRequest(
            className = functionOwner.name,
            functionName = functionName,
            classTypesOfFunctionParameter = argumentTypes.map { it.name },
            valuesOfFunctionParameter = argumentValue
        )
        return when (val result = this.rpcInterface.invoke(request)) {
            is AndroidParcelableInvocationResponse -> {
                val throwable = result.throwable
                if (throwable != null) {
                    throw Throwable(throwable)
                }
                result.data
            }
            else -> null
        }
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
        val oneShotContinuation = OneShotContinuation(continuation)
        val deathListener = object : RPCInterface.DeathListener {
            override fun onConnectionLoss() {
                this@RPCorrelatorProxy.rpcInterface.removeDeathListener(this)
                oneShotContinuation.resume(null)
            }
        }
        val rpCallback = RPCallback { data, throwable ->
            this@RPCorrelatorProxy.rpcInterface.removeDeathListener(deathListener)
            if (throwable != null) {
                oneShotContinuation.resumeWithException(Throwable(throwable))
            } else oneShotContinuation.resume(data)
        }
        val request = AndroidSuspendInvocationRequest(
            className = functionOwner.name,
            functionName = functionName,
            classTypesOfFunctionParameter = argumentTypes.map { it.name },
            valuesOfFunctionParameter = argumentValue,
            rpCallback = rpCallback
        )
        this.rpcInterface.addDeathListener(deathListener)
        return@suspendCoroutineUninterceptedOrReturn when (val result = this.rpcInterface.invoke(request)) {
            is AndroidParcelableInvocationResponse -> {
                val throwable = result.throwable
                if (throwable != null) {
                    throw Throwable(throwable)
                }
                result.data
            }
            is AndroidParcelableInvocationInternalErrorResponse -> COROUTINE_SUSPENDED
            else -> throw UnsupportedOperationException("unSupported ResponseType: ${result.javaClass}")
        }
    }
}

private class RPCorrelatorStub(private val rpCorrelator: RPCorrelator) : RPCorrelator by rpCorrelator {

    override val isOpen: Boolean get() = true

    val rpcInterface = RPCInterface { aidlRequest ->
        val result = kotlin.runCatching {
            when (aidlRequest) {
                is AndroidInvocationRequest -> this.callFunction(
                    functionOwner = aidlRequest.className.stringTypeConvert,
                    functionName = aidlRequest.functionName,
                    argumentTypes = aidlRequest.classTypesOfFunctionParameter.stringTypeConvert,
                    argumentValue = aidlRequest.valuesOfFunctionParameter,
                )
                is AndroidSuspendInvocationRequest -> {
                    val continuation = object : Continuation<Any?> {
                        override val context: CoroutineContext get() = Dispatchers.Default
                        override fun resumeWith(result: Result<Any?>) = aidlRequest.rpCallback.callback(result.getOrNull(), result.exceptionOrNull())
                    }
                    val oneShotContinuation = OneShotContinuation(continuation)
                    (this::callSuspendFunction as Function5<Class<*>, String, List<Class<*>>, List<Any?>, Continuation<Any?>, Any?>)
                        .invoke(aidlRequest.className.stringTypeConvert, aidlRequest.functionName, aidlRequest.classTypesOfFunctionParameter.stringTypeConvert, aidlRequest.valuesOfFunctionParameter, oneShotContinuation)
                }
                is AttachReCorrelatorRequest -> this.attachCorrelator(correlator = aidlRequest.rpCorrelator)
                else -> null
            }
        }
        AndroidParcelableInvocationResponse(data = result.getOrNull()?.safeUnbox(), throwable = result.exceptionOrNull())
    }
}