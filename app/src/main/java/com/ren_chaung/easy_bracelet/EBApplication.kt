package com.ren_chaung.easy_bracelet

import android.app.Application
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.utils.CrashHandler
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
        CrashHandler.init(this)
        //BraceletMachineManager.isDebug = BuildConfig.DEBUG
        SoundHelper.loadSound(this)
        BraceletMachineManager.bind(this)
    }
}
