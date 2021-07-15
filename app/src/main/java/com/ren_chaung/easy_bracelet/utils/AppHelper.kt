package com.ren_chaung.easy_bracelet.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ren_chaung.easy_bracelet.view.activity.MainActivity
import kotlin.system.exitProcess

//应用助手
object AppHelper {

    fun restartApp(context: Context?) {
        context?.let {
            val intent = Intent()
            intent.setClass(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}