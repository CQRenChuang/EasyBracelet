package com.comocm.base.extension

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


fun String.toCalendar(): Calendar? {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.parse(this) ?: return null
        val cal = Calendar.getInstance() ?: return null
        cal.time = date
        return cal
    } catch (e: Exception) {
        return null
    }
}

fun String.isMobile(): Boolean {
    val mainRegex = "^(1[3-9])\\d{9}$"
    val p = Pattern.compile(mainRegex)
    val m = p.matcher(this)
    return m.matches()
}

// Extension property on Exception
val Exception.stackTraceString: String
    get() {
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }

