package com.ocm.bracelet_machine_sdk.model

import android.content.Context
import android.content.SharedPreferences
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.R

class NumModel(private val addrIndex: Int) {
    //机器最大数量
    var maxNum = 120
        set(value) {
            sharedPreferences?.edit()?.putInt("maxNum_${addrIndex}", value)?.apply()
            field = value
        }
    //当前数量
    var currentNum = 100
        set(value) {
            sharedPreferences?.edit()?.putInt("currentNum_${addrIndex}", value)?.apply()
            if (value <= 0) listener?.onNoBracelet(addrIndex)
            else if(!isDisableRestart) {
                listener?.onNeedRestart(addrIndex)
            }
            field = value
            listener?.onCurrentNumChange(addrIndex, value)
        }

    var sharedPreferences: SharedPreferences? = null
    private var isDisableRestart = false
    var listener: NumberListener? = null

    fun loadForSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
        sharedPreferences?.let { sp ->
            maxNum = sp.getInt("maxNum_${addrIndex}", 120)
            currentNum = sp.getInt("currentNum_${addrIndex}", 100)
        }
    }

    /**
     * 当前数量加1
     */
    fun addCurrentNum() {
        if (!BraceletMachineManager.enableCalc) return
        isDisableRestart = false
        currentNum++
    }

    /**
     * 当前数量减1
     */
    fun desCurrentNum() {
        if (!BraceletMachineManager.enableCalc) return
        isDisableRestart = true
        if (currentNum > 0) currentNum--
        isDisableRestart = false
    }

    interface NumberListener {
        fun onNoBracelet(addr: Int)
        fun onNeedRestart(addr: Int)
        fun onCurrentNumChange(addr: Int, num: Int)
    }
}