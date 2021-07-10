package com.ren_chaung.easy_bracelet

import android.app.Application
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta

/**
 * Created by ocm on 2018-09-11.
 */
class EBApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Bugly.init(applicationContext, "e6afa835a6", false)
        Beta.autoCheckUpgrade = false
        Beta.initDelay = 0
//        CrashHandler.getInstance().init(this)
        LocalLogger.isDebug = true
//        BraceletMachineManager.bind(this)
    }
}