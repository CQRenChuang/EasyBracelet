package com.ren_chaung.easy_bracelet

import android.app.Application
import com.comocm.sound.SoundHelper
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
        Bugly.init(applicationContext, "4a6b142471", false)
        Beta.autoCheckUpgrade = false
        Beta.initDelay = 0
//        CrashHandler.getInstance().init(this)
        LocalLogger.isDebug = true
        SoundHelper.loadSound(this)
        BraceletMachineManager.bind(this)
        BraceletMachineManager.isDebug = BuildConfig.DEBUG
    }
}