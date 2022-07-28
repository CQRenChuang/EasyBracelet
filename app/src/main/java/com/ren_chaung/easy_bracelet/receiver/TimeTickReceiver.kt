package com.ren_chaung.easy_bracelet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ren_chaung.easy_bracelet.utils.DateTimeHelper
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import kotlinx.android.synthetic.main.activity_base_fragment.*
import java.util.*


//时间改变广播
class TimeTickReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_TIME_TICK) {
            (context as? BaseFragmentActivity)?.let {
                it.tvTime.text = DateTimeHelper.getTime()
            }
        }
    }
}