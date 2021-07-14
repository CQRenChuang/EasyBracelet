package com.ocm.bracelet_machine_sdk

import android.content.Context
import android.content.SharedPreferences
import com.ocm.bracelet_machine_sdk.R

/**
 * 手环数量管理
 */
internal object BraceletNumberManager {
    //机器最大数量
    var maxNum = 120
        set(value) {
            sharedPreferences?.edit()?.putInt("maxNum", value)?.apply()
            field = value
        }
    //当前数量
    var currentNum = 100
        set(value) {
            sharedPreferences?.edit()?.putInt("currentNum", value)?.apply()
            if (value <= 0) listener?.onNoBracelet()
            else if(!isDisableRestart) {
                listener?.onNeedRestart()
            }
            field = value
            listener?.onCurrentNumChange(value)
        }
    var sharedPreferences: SharedPreferences? = null
    var listener: BraceletNumberManagerListener? = null
    private var isDisableRestart = false

    fun loadForSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
        sharedPreferences?.let { sp ->
            maxNum = sp.getInt("maxNum", 120)
            currentNum = sp.getInt("currentNum", 100)
        }
    }

    /**
     * 当前数量加1
     */
    fun addCurrentNum() {
        isDisableRestart = false
        currentNum++
    }

    /**
     * 当前数量减1
     */
    fun desCurrentNum() {
        isDisableRestart = true
        if (currentNum > 0) currentNum--
        isDisableRestart = false
    }

    interface BraceletNumberManagerListener {
        fun onNoBracelet()
        fun onNeedRestart()
        fun onCurrentNumChange(num: Int)
    }
}