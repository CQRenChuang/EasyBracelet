package com.ren_chaung.easy_bracelet.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.CheckGiveBackCallback
import com.ocm.bracelet_machine_sdk.FetchCallback
import com.ocm.bracelet_machine_sdk.GiveBackCallback
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.dialog.ResultTipsDialog
import kotlinx.android.synthetic.main.fragment_fetch.view.*

/**
 * 基础Fragment，都加上手环机回调监听
 */
class FetchFragment : BaseFragment(), FetchCallback {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        SoundHelper.beginFetch()
        return inflater.inflate(R.layout.fragment_fetch, container, false).apply {
            BraceletMachineManager.fetchBracelet(this@FetchFragment)
        }
    }

    override fun onBeginFetch() {
        setTitle("取手环")
    }

    override fun onBack() {
        BraceletMachineManager.stopFetch()
    }

    override fun onFetchSuccess(no: String) {
        showToast(no)
        SoundHelper.endFetch()
        popFragment()
    }

    override fun onFetchFail(msg: String) {
        if (msg.contains("剩余手环")) {
            SoundHelper.noBrand()
        }
        context?.let { ResultTipsDialog.Builder(it, ResultTipsDialog.Builder.State.ERROR, msg).create().show{
            popFragment()
        } }
    }
}
