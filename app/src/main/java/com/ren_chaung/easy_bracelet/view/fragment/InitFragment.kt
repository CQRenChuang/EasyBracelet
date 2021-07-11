package com.ren_chaung.easy_bracelet.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import kotlinx.android.synthetic.main.fragment_init.view.*

/**
 * 基础Fragment，都加上手环机回调监听
 */
class InitFragment(private val listener: InitFragmentListener) : BaseFragment() {



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_init, container, false).apply {
            ivLoading.setOnSingleClickListener {
                listener.initSuccess()
            }
        }
    }

    interface InitFragmentListener {
        fun initSuccess()
    }
}
