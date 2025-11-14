package com.dongxingx.logger.utils

import android.app.Activity

object ScreenAdaptUtil {
    fun setCustomDensity(activity: Activity, width: Int = 360) {
        activity.resources.displayMetrics.apply {
            val targetDensity = widthPixels.toFloat() / width
            density = targetDensity
            scaledDensity = targetDensity
            densityDpi = (160 * targetDensity).toInt()
        }
    }
}