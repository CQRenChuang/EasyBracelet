package com.ren_chaung.easy_bracelet.utils.extension

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

fun View.setAllEnabled(enabled: Boolean) {
    isEnabled = enabled
    if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
}
