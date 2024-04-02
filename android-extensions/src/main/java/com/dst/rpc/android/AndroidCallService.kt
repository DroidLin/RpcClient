package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.CallService
import com.dst.rpc.ClientManager
import com.dst.rpc.INoProguard
import com.dst.rpc.OneShotContinuation
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author liuzhongao
 * @since 2024/3/3 16:22
 */
internal interface AndroidCallService : CallService {

    fun attachCallService(callService: AndroidCallService)

    class Proxy(val aidlFunction: AIDLFunction) : AndroidCallService {

        override val isOpen: Boolean get() = aidlFunction.isAlive

        override fun attachCallService(callService: AndroidCallService) {
            val request = AttachAndroidCallServiceRequest(callService)
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
            val result = this.aidlFunction.invoke(request)
            val throwable = result.throwable
            if (throwable != null) {
                throw Throwable(throwable)
            }
            return result.data
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
                    this@Proxy.aidlFunction.removeDeathListener(this)
                    oneShotContinuation.resume(null)
                }
            }
            val rpCallback = AIDLCallback(AIDLCallback { data, throwable ->
                this@Proxy.aidlFunction.removeDeathListener(deathListener)
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
                aidlCallback = rpCallback
            )
            this.aidlFunction.addDeathListener(deathListener)
            val result = this.aidlFunction.invoke(request)
            val throwable = result.throwable
            if (throwable != null) {
                this.aidlFunction.removeDeathListener(deathListener)
                throw Throwable(throwable)
            }
            result.data
        }
    }

    class Stub(private val callService: AndroidCallService) : AndroidCallService by callService {

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
}

internal fun AndroidCallService(function: AIDLFunction): AndroidCallService =
    AndroidCallService.Proxy(function)

internal fun AndroidCallService(callService: AndroidCallService): AndroidCallService =
    AndroidCallService.Stub(callService)

internal val AndroidCallService.iBinder: IBinder
    get() = when (this) {
        is AndroidCallService.Proxy -> this.aidlFunction.iBinder
        is AndroidCallService.Stub -> this.aidlFunction.iBinder
        else -> throw IllegalArgumentException("unknown type of current AndroidCallService: ${this.javaClass.name}")
    }
