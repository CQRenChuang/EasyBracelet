package com.ren_chaung.easy_bracelet.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ren_chaung.easy_bracelet.R

/**
 * 基础Fragment，都加上手环机回调监听
 */
class SettingFragment : BaseFragment() {



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setTitle("手环机设置")
        return inflater.inflate(R.layout.fragment_setting, container, false).apply {
        }
    }
}
