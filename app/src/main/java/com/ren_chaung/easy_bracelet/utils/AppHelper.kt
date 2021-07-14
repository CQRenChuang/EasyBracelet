package com.ren_chaung.easy_bracelet.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

//应用助手
object AppHelper {

    fun restartApp(context: Context?, cls: Class<*>) {
        context?.let {
            val mStartActivity = Intent(it, cls)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent.getActivity(
                it,
                mPendingIntentId,
                mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr =
                it.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
            exitProcess(0)
        }
    }
}