package com.comocm.base.extension

import java.math.RoundingMode
import java.text.DecimalFormat

fun Float.toMoney(): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(this)
}

fun Double.toMoney(): String {
        return this.toFloat().toMoney()
}