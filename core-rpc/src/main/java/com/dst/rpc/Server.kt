package com.dst.rpc

import java.util.ServiceLoader

/**
 * @author liuzhongao
 * @since 2024/3/1 00:50
 */
interface Server<P : Request, T : Response> {

    fun isSupported(request: Request): Boolean

    fun execute(request: P, callback: Callback<T>)

    companion object : Server<Request, Response> {

        private val remoteServerImplList: MutableList<Server<*, *>> = ArrayList()

        override fun isSupported(request: Request): Boolean = true

        override fun execute(request: Request, callback: Callback<Response>) {
            this.collectServerImplementations()
            val targetServer = getServer<Request, Response>(request = request)
            if (targetServer != null) {
                targetServer.execute(request = request, callback)
            } else callback.callback(response = Response)
        }

        private fun collectServerImplementations() {
            if (this.remoteServerImplList.isEmpty()) {
                synchronized(this) {
                    if (this.remoteServerImplList.isEmpty()) {
                        this.remoteServerImplList += ServiceLoader.load(Server::class.java).toList()
                    }
                }
            }
            if (this.remoteServerImplList.isEmpty()) {
                throw IllegalStateException("No server implementation found.")
            }
        }

        private fun <P : Request, T : Response> getServer(request: Request): Server<P, T>? =
            synchronized(this) { this.remoteServerImplList.find { it.isSupported(request = request) } as? Server<P, T> }
    }
}