package com.ren_chaung.easy_bracelet.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {

    fun getTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)
        return (if (hour < 10) "0$hour" else hour).toString() + ":" + if (min < 10) "0$min" else min
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateLogStr(): String {
        val date = Date()
        val format = SimpleDateFormat("yyyyMMdd")
        return format.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(): String {
        val date = Date()
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}