package com.ocm.bracelet_machine_sdk.utils

import android.util.Log
import com.ocm.bracelet_machine_sdk.BuildConfig
import floatwindow.xishuang.float_lib.FloatLoger

object LocalLogger {
    var isDebug: Boolean = false

    fun write(msg: String) {
        if(isDebug) Log.d("Local-Logger", msg)
        FloatLoger.getInstance().writeLog("${BuildConfig.VERSION_NAME} - $msg")
    }
}