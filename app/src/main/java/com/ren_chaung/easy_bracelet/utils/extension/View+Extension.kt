package com.comocm.base.extension

import android.view.View
import com.comocm.base.listener.OnMultiClickListener
import com.comocm.base.listener.OnSingleClickListener

/**
 * 按钮一秒内按多次只响应一次
 * @receiver View
 * @param onClick Function1<[@kotlin.ParameterName] View, Unit>
 */
fun View.setOnSingleClickListener(onClick: ((v: View)->Unit)) {
    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View) {
            onClick(v)
        }
    })
}

fun View.setOnDoubleClickListener(onClick: ((v: View)->Unit)) {
    setOnClickListener(object : OnMultiClickListener() {
        override fun onMultiClick(v: View) {
            onClick(v)
        }
    })
}

fun View.setOnMultiClickListener(clickNum: Int, time: Long = 1000L, onClick: ((v: View)->Unit)) {
    setOnClickListener(object : OnMultiClickListener(clickNum, time) {
        override fun onMultiClick(v: View) {
            onClick(v)
        }
    })
}