package com.ren_chaung.easy_bracelet.utils

import android.util.Log
import floatwindow.xishuang.float_lib.BuildConfig
import floatwindow.xishuang.float_lib.FloatLoger

object LocalLogger {
    var isDebug: Boolean = false
    var version: String = ""

    fun write(msg: String) {
        if(isDebug) Log.d("Local-Logger", msg)
        FloatLoger.getInstance().writeLog("$version - $msg")
    }
}