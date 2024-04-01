package com.dst.rpc.android

import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard
import com.dst.rpc.OneShotContinuation
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

internal fun AndroidCallService(callService: AndroidCallService): AndroidCallService = AndroidCallServiceStub(callService)

internal class AndroidCallServiceStub(private val callService: AndroidCallService) : AndroidCallService by callService {

    override val isOpen: Boolean get() = true

    val aidlFunction = AIDLFunction { aidlRequest ->
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
                        override fun resumeWith(result: Result<Any?>) = aidlRequest.aidlCallback.callback(result.getOrNull(), result.exceptionOrNull())
                    }
                    val oneShotContinuation = OneShotContinuation(continuation)
                    (this::callSuspendFunction as Function6<Class<*>, String, String, List<Class<*>>, List<Any?>, Continuation<Any?>, Any?>)
                        .invoke(aidlRequest.className.stringTypeConvert, aidlRequest.functionName, aidlRequest.functionUniqueKey, aidlRequest.classTypesOfFunctionParameter.stringTypeConvert, aidlRequest.valuesOfFunctionParameter, oneShotContinuation)
                }
                is AttachAndroidCallServiceRequest -> this.attachCallService(callService = aidlRequest.callService)
                else -> null
            }
        }
        AndroidParcelableInvocationResponse(data = result.getOrNull(), throwable = result.exceptionOrNull())
    }

    override fun callFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        return ClientManager.getService(functionOwner as Class<INoProguard>, functionUniqueKey.isNotEmpty())
            .invokeNonSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes, argumentValue)
    }

    override suspend fun callSuspendFunction(
        functionOwner: Class<*>,
        functionName: String,
        functionUniqueKey: String,
        argumentTypes: List<Class<*>>,
        argumentValue: List<Any?>
    ): Any? {
        return ClientManager.getService(functionOwner as Class<INoProguard>, functionUniqueKey.isNotEmpty())
            .invokeSuspendFunction(functionOwner, functionName, functionUniqueKey, argumentTypes + Continuation::class.java, argumentValue)
    }
}