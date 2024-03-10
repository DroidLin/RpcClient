package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.INoProguard
import java.util.*

/**
 * @author liuzhongao
 * @since 2024/3/2 01:14
 */
internal interface AIDLFunction : INoProguard {

    val isAlive: Boolean

    fun invoke(request: Request): Response

    fun addDeathListener(deathListener: DeathListener) {}

    fun removeDeathListener(deathListener: DeathListener) {}

    fun interface DeathListener {

        fun onConnectionLoss()
    }
}

internal val AIDLFunction.iBinder: IBinder
    get() = when (this) {
        is AIDLFunctionImpl -> this.function.asBinder()
        is AIDLFunctionImplStub -> this.function.asBinder()
        else -> throw IllegalArgumentException("unknown type of current AIDLFunction: ${this.javaClass.name}")
    }

internal fun AIDLFunction(function: Function): AIDLFunction = AIDLFunctionImpl(function = function)

internal fun AIDLFunction(block: (Request) -> Response): AIDLFunction {
    return object : AIDLFunctionImplStub() {
        override fun invoke(request: Request): Response = block(request)
    }
}

private class AIDLFunctionImpl(val function: Function) : AIDLFunction {

    private val deathListenerList = LinkedList<AIDLFunction.DeathListener>()
    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            this@AIDLFunctionImpl.function.asBinder().unlinkToDeath(this, 0)
            val tempDeathListenerList = synchronized(this@AIDLFunctionImpl) {
                this@AIDLFunctionImpl.deathListenerList.toList()
            }
            tempDeathListenerList.forEach { it.onConnectionLoss() }
        }
    }

    override val isAlive: Boolean get() = this.function.asBinder().isBinderAlive

    init {
        this.function.asBinder().linkToDeath(this.deathRecipient, 0)
    }

    override fun invoke(request: Request): Response {
        val transportBridge = TransportBridge.obtain()
        var response: Response? = null
        try {
            transportBridge.request = request
            this.function.invoke(transportBridge)
            response = transportBridge.response as? Response
        } catch (throwable: Throwable) {
            response = AndroidParcelableInvocationInternalErrorResponse(throwable)
        } finally {
            TransportBridge.recycle(transportBridge)
        }
        return response ?: Response
    }

    override fun addDeathListener(deathListener: AIDLFunction.DeathListener) {
        synchronized(this) {
            this.deathListenerList += deathListener
        }
    }

    override fun removeDeathListener(deathListener: AIDLFunction.DeathListener) {
        synchronized(this) {
            this.deathListenerList -= deathListener
        }
    }
}

private abstract class AIDLFunctionImplStub : AIDLFunction {

    val function = object : Function.Stub() {
        override fun invoke(bridge: TransportBridge?) {
            val request = bridge?.request
            bridge?.response = if (request != null && request is Request) {
                this@AIDLFunctionImplStub.invoke(request)
            } else Response
        }
    }

    override val isAlive: Boolean get() = true
}