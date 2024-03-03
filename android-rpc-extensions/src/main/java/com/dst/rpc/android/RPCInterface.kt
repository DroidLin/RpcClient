package com.dst.rpc.android

import android.os.IBinder
import com.dst.rpc.INoProguard
import com.dst.rpc.Response
import java.util.*

/**
 * @author liuzhongao
 * @since 2024/3/2 01:14
 */
internal interface RPCInterface : INoProguard {

    val isAlive: Boolean

    fun invoke(request: AIDLRequest): AIDLResponse

    fun addDeathListener(deathListener: DeathListener) {}

    fun removeDeathListener(deathListener: DeathListener) {}

    fun interface DeathListener {

        fun onConnectionLoss()
    }
}

internal val RPCInterface.iBinder: IBinder
    get() = when (this) {
        is RPCInterfaceImpl -> this.function.asBinder()
        is RPCInterfaceImplStub -> this.function.asBinder()
        else -> throw IllegalArgumentException("unknown type of current RPCInterface: ${this.javaClass.name}")
    }

internal fun RPCInterface(function: Function): RPCInterface = RPCInterfaceImpl(function = function)

internal fun RPCInterface(block: (AIDLRequest) -> AIDLResponse): RPCInterface {
    return object : RPCInterfaceImplStub() {
        override fun invoke(request: AIDLRequest): AIDLResponse = block(request)
    }
}

private class RPCInterfaceImpl(val function: Function) : RPCInterface {

    private val deathListenerList = LinkedList<RPCInterface.DeathListener>()
    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            this@RPCInterfaceImpl.function.asBinder().unlinkToDeath(this, 0)
            val tempDeathListenerList = synchronized(this@RPCInterfaceImpl) {
                this@RPCInterfaceImpl.deathListenerList.toList()
            }
            tempDeathListenerList.forEach { it.onConnectionLoss() }
        }
    }

    override val isAlive: Boolean get() = this.function.asBinder().isBinderAlive

    init {
        this.function.asBinder().linkToDeath(this.deathRecipient, 0)
    }

    override fun invoke(request: AIDLRequest): AIDLResponse {
        val transportBridge = TransportBridge.obtain()
        var response: AIDLResponse? = null
        try {
            transportBridge.request = request
            this.function.invoke(transportBridge)
            response = transportBridge.response as? AIDLResponse
        } catch (throwable: Throwable) {
            response = AndroidParcelableInvocationInternalErrorResponse(throwable)
        } finally {
            TransportBridge.recycle(transportBridge)
        }
        return response ?: AIDLResponse
    }

    override fun addDeathListener(deathListener: RPCInterface.DeathListener) {
        synchronized(this) {
            this.deathListenerList += deathListener
        }
    }

    override fun removeDeathListener(deathListener: RPCInterface.DeathListener) {
        synchronized(this) {
            this.deathListenerList -= deathListener
        }
    }
}

private abstract class RPCInterfaceImplStub : RPCInterface {

    val function = object : Function.Stub() {
        override fun invoke(bridge: TransportBridge?) {
            val request = bridge?.request
            bridge?.response = if (request != null && request is AIDLRequest) {
                this@RPCInterfaceImplStub.invoke(request)
            } else Response
        }
    }

    override val isAlive: Boolean get() = true
}