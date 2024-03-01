package com.dst.rpc.android;

import com.dst.rpc.android.Bridge;

interface Function {

    void invoke(inout Bridge bridge);
}