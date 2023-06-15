package com.ren_chaung.easy_bracelet.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import kotlinx.android.synthetic.main.fragment_main2.view.layoutFetch
import kotlinx.android.synthetic.main.fragment_main2.view.layoutGiveBack

/**
 * 基础Fragment，都加上手环机回调监听
 */
class MainFragment2 : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setTitle(resources.getString(R.string.app_name))
        return inflater.inflate(R.layout.fragment_main2, container, false).apply {
            if (!BraceletMachineManager.checkSelfNo1) {
                layoutFetch.visibility = View.GONE
            }
            if (!BraceletMachineManager.checkSelfNo2) {
                layoutGiveBack.visibility = View.GONE
            }
            layoutFetch.setOnSingleClickListener {
                push(FetchFragment2(0))
            }
            layoutGiveBack.setOnSingleClickListener {
                push(FetchFragment2(1))
            }
            requestFocus()
        }
    }

}
