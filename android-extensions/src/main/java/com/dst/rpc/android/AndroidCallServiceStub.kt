package com.dst.rpc.android

import com.dst.rpc.OneShotContinuation
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

internal fun AndroidCallService(callService: AndroidCallService): AndroidCallService = AndroidCallServiceStub(callService)

internal class AndroidCallServiceStub(private val callService: AndroidCallService) : AndroidCallService by callService {

    override val isOpen: Boolean get() = true

    val rpcInterface = RPCInterface { aidlRequest ->
        val result = kotlin.runCatching {
            when (aidlRequest) {
                is AndroidInvocationRequest -> this.callFunction(
                    functionOwner = aidlRequest.className.stringTypeConvert,
                    functionName = aidlRequest.functionName,
                    functionUniqueKey = aidlRequest.functionUniqueKey,
                    argumentTypes = aidlRequest.classTypesOfFunctionParameter.stringTypeConvert,
                    argumentValue = aidlRequest.valuesOfFunctionParameter,
                )
                is AndroidSuspendInvocationRequest -> {
                    val continuation = object : Continuation<Any?> {
                        override val context: CoroutineContext get() = Dispatchers.Default
                        override fun resumeWith(result: Result<Any?>) = aidlRequest.AIDLCallback.callback(result.getOrNull(), result.exceptionOrNull())
                    }
                    val oneShotContinuation = OneShotContinuation(continuation)
                    (this::callSuspendFunction as Function6<Class<*>, String, String, List<Class<*>>, List<Any?>, Continuation<Any?>, Any?>)
                        .invoke(aidlRequest.className.stringTypeConvert, aidlRequest.functionName, aidlRequest.functionUniqueKey, aidlRequest.classTypesOfFunctionParameter.stringTypeConvert, aidlRequest.valuesOfFunctionParameter, oneShotContinuation)
                }
                is AttachReCorrelatorRequest -> this.attachCallService(callService = aidlRequest.rpCorrelator)
                else -> null
            }
        }
        AndroidParcelableInvocationResponse(data = result.getOrNull(), throwable = result.exceptionOrNull())
    }
}