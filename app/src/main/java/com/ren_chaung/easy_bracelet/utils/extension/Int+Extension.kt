package com.comocm.base.extension

import android.content.res.Resources
import java.math.RoundingMode
import java.text.DecimalFormat

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()