package com.ren_chaung.easy_bracelet.utils.listener

import android.util.Log
import android.view.View

//单词点击事件，防止1秒内点击多次
abstract class OnSingleClickListener : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var timeInterval = 1000L

    constructor()

    constructor(interval: Long) {
        this.timeInterval = interval
    }

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > timeInterval) {
            // 单次点击事件
            onSingleClick(v)
            mLastClickTime = nowTime
        }
    }

    protected abstract fun onSingleClick(v: View)
}

//多次点击事件，默认两次
abstract class OnMultiClickListener : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var timeInterval = 1000L
    private var targetClickNum = 2
    private var clickCount = 0

    constructor()

    constructor(interval: Long) {
        this.timeInterval = interval
    }

    constructor(targetClickNum: Int, interval: Long) {
        this.timeInterval = interval
        this.targetClickNum = targetClickNum
    }

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        when {
            clickCount == 0 -> {
                mLastClickTime = nowTime
                ++clickCount
            }
            nowTime - mLastClickTime > timeInterval -> {
                clickCount = 0
            }
            clickCount == targetClickNum-1 -> {
                clickCount = 0
                onMultiClick(v)
            }
            else -> {
                ++clickCount
            }
        }
    }

    protected abstract fun onMultiClick(v: View)
}