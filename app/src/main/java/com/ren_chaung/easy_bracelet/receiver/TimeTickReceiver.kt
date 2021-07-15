package com.ren_chaung.easy_bracelet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*


//时间改变广播
class TimeTickReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_TIME_TICK) {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
        }
    }
}