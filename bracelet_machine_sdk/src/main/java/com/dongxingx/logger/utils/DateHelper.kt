package com.dongxingx.logger.utils

import java.text.SimpleDateFormat
import java.util.Date

object DateHelper {

    fun strToDate(str: String): Date? {
        return strToDate(str, "yyyy-MM-dd")
    }

    fun strToDate(str: String, format: String): Date? {
        val formatter = SimpleDateFormat(format)
        return formatter.parse(str)
    }

    fun getDateTime(date: Date): String {
        return getDateTime(date, "yyyy-MM-dd HH:mm:ss")
    }

    fun getDateTime(): String {
        return getDateTime(Date(), "yyyy-MM-dd HH:mm")
    }

    fun getTime(): String {
        return getTime(Date())
    }

    fun getTime(date: Date): String {
        return getDateTime(date, "HH:mm")
    }

    fun getDateTime(date: Date, format: String): String {
        val format = SimpleDateFormat(format)
        return format.format(date)
    }

    fun getDate(): String {//可根据需要自行截取数据显示
        return getDate(Date())
    }

    fun getDate(date: Date): String {//可根据需要自行截取数据显示
        return getDateTime(date, "yyyy-MM-dd")
    }

    fun getRemainSecondsOneDay(): Int {
        val currentDate = Date()
        val dayH = 3600*24
        val nowSec = currentDate.hours*60*60 + currentDate.minutes*60 + currentDate.seconds
        return dayH - nowSec
    }
}