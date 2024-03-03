package com.dst.rpc.android;

import com.dst.rpc.android.TransportBridge;

interface Function {

    void invoke(inout TransportBridge bridge);
}