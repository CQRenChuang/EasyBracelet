package com.ren_chaung.easy_bracelet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.utils.AppHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {     // boot
            BraceletMachineManager.loadData(context)
            if (BraceletMachineManager.enableAutoRun) {
                AppHelper.restartApp(context)
            }
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.coachclass.USB_PERMISSION"
        const val ACTION_PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED"
    }
}