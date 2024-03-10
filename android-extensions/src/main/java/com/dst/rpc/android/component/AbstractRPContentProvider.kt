package com.dst.rpc.android.component

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.dst.rpc.android.AIDLClient
import com.dst.rpc.android.AIDLConnection
import com.dst.rpc.android.AIDLRPCAddress
import com.dst.rpc.android.RPContext
import kotlinx.coroutines.CompletableDeferred

/**
 * @author liuzhongao
 * @since 2024/3/2 19:19
 */
abstract class AbstractRPContentProvider : ContentProvider() {

    protected abstract val addressOfCurrentProvider: AIDLRPCAddress

    protected abstract fun onReceiveRPConnection(context: RPContext)

    override fun onCreate(): Boolean = true

    override fun call(authority: String, method: String, arg: String?, extras: Bundle?): Bundle? {
        if (authority != addressOfCurrentProvider.domain && method != "connection") {
            return null
        }
        val rpContext = extras?.rpcContext ?: return null
        if (this.addressOfCurrentProvider == rpContext.remoteAddress) {
            this.onReceiveRPConnection(context = rpContext)
            AIDLClient.acceptConnection(rpContext.sourceAddress, CompletableDeferred(AIDLConnection(rpContext.rpCorrelator)))
            AIDLClient.twoWayConnectionEstablish(rpContext.rpCorrelator)
        }
        return null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}