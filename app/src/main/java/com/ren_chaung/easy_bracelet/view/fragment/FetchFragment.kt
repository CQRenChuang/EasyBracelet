package com.ren_chaung.easy_bracelet.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.FetchCallback
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.dialog.ResultTipsDialog
import kotlinx.android.synthetic.main.dialog_result_tips.*
import kotlinx.android.synthetic.main.fragment_fetch.view.*

/**
 * 基础Fragment，都加上手环机回调监听
 */
class FetchFragment : BaseFragment(), FetchCallback {

    private var canGoBack = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        SoundHelper.beginFetch()
        return inflater.inflate(R.layout.fragment_fetch, container, false).apply {
            if (BraceletMachineManager.fetchNum == 1) {
                BraceletMachineManager.fetchBracelet(this@FetchFragment)
            } else {
                BraceletMachineManager.fetchMultiBracelet(BraceletMachineManager.fetchNum, this@FetchFragment)
            }
        }
    }

    override fun onBeginFetch() {
        setTitle("取手环")
    }

    override fun canGoBack(): Boolean {
        if (!canGoBack) {
            showToast("操作中断中，即将返回")
            BraceletMachineManager.stopFetch()
        }
        return canGoBack
    }

    override fun onFetchSuccess(no: String, hexNo: String) {
        showToast("$no\n$hexNo")
    }

    override fun onCompleted() {
        SoundHelper.endFetch()
        canGoBack = true
        popFragment()
    }

    override fun onFetchFail(msg: String) {
        if (msg.contains("剩余手环")) {
            SoundHelper.noBrand()
        } else if(msg.contains("手环异常")) {
            SoundHelper.exceptionCard()
        }
        context?.let { ResultTipsDialog.Builder(it, ResultTipsDialog.Builder.State.ERROR, msg).create().show{
            canGoBack = true
            popFragment()
        } }
    }

    override fun onStopBack() {
        canGoBack = true
        popFragment()
    }

    override fun onReceiveTimeout() {
        context?.let {
            ResultTipsDialog.Builder(it, ResultTipsDialog.Builder.State.FAIL, "无响应，请重试").create().apply {
                buttonCancel.visibility = View.VISIBLE
                buttonCancel.setOnSingleClickListener {
                    dismiss()
                    onStopBack()
                }
                buttonDone.text = "重试"
                buttonDone.setOnSingleClickListener {
                    dismiss()
                    BraceletMachineManager.fetchBracelet(this@FetchFragment)
                }
            }.show()
        }
    }
}
