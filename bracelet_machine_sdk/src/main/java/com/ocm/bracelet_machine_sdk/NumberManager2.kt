package com.ocm.bracelet_machine_sdk

import android.content.Context
import android.content.SharedPreferences
import com.ocm.bracelet_machine_sdk.model.NumModel

/**
 * 手环数量管理
 */
internal object NumberManager2 {

    val numList = arrayOf(NumModel(0), NumModel(1))
    var sharedPreferences: SharedPreferences? = null

    fun loadForSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
        numList.forEach { it.loadForSharedPreferences(context) }
    }

    /**
     * 当前数量加1
     */
    fun addCurrentNum(addrIndex: Int) {
        numList.getOrNull(addrIndex)?.addCurrentNum()
    }

    /**
     * 当前数量减1
     */
    fun desCurrentNum(addrIndex: Int) {
        numList.getOrNull(addrIndex)?.desCurrentNum()
    }
}