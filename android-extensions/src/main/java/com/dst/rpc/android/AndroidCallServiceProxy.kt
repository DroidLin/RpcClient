package com.dst.rpc.android

import com.dst.rpc.OneShotContinuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal fun AndroidCallService(function: AIDLFunction): AndroidCallService = AndroidCallServiceProxy(function)

internal class AndroidCallServiceProxy(val aidlFunction: AIDLFunction) : AndroidCallService {

    override val isOpen: Boolean get() = aidlFunction.isAlive

    override fun attachCallService(callService: AndroidCallService) {
        val request = AttachReCorrelatorRequest(callService)
        this.aidlFunction.invoke(request = request)
    }

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        val request = AndroidInvocationRequest(
            className = functionOwner.name,
            functionName = functionName,
            functionUniqueKey = functionUniqueKey,
            classTypesOfFunctionParameter = argumentTypes.map { it.name },
            valuesOfFunctionParameter = argumentValue
        )
        return when (val result = this.aidlFunction.invoke(request)) {
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
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
        val oneShotContinuation = OneShotContinuation(continuation)
        val deathListener = object : AIDLFunction.DeathListener {
            override fun onConnectionLoss() {
                this@AndroidCallServiceProxy.aidlFunction.removeDeathListener(this)
                oneShotContinuation.resume(null)
            }
        }
        val rpCallback = AIDLCallback(AIDLCallback { data, throwable ->
            this@AndroidCallServiceProxy.aidlFunction.removeDeathListener(deathListener)
            if (throwable != null) {
                oneShotContinuation.resumeWithException(Throwable(throwable))
            } else oneShotContinuation.resume(data)
        })
        val request = AndroidSuspendInvocationRequest(
            className = functionOwner.name,
            functionName = functionName,
            functionUniqueKey = functionUniqueKey,
            classTypesOfFunctionParameter = argumentTypes.map { it.name },
            valuesOfFunctionParameter = argumentValue,
            AIDLCallback = rpCallback
        )
        this.aidlFunction.addDeathListener(deathListener)
        return@suspendCoroutineUninterceptedOrReturn when (val result = this.aidlFunction.invoke(request)) {
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
