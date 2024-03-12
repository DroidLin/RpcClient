package com.dst.rpc.android

import android.content.Intent
import android.os.Bundle

interface OnPreAttachCallback {
    fun onPreAttachBroadcastIntent(intent: Intent) {}
    fun onPreAttachServiceLaunchIntent(intent: Intent) {}
    fun onPreAttachContentProviderBundle(bundle: Bundle) {}
}