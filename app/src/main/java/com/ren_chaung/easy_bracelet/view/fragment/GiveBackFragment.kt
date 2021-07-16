package com.ren_chaung.easy_bracelet.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.CheckGiveBackCallback
import com.ocm.bracelet_machine_sdk.GiveBackCallback
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.dialog.ResultTipsDialog
import kotlinx.android.synthetic.main.fragment_give_back.view.*

/**
 * 基础Fragment，都加上手环机回调监听
 */
class GiveBackFragment : BaseFragment(), GiveBackCallback {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setTitle("还手环")
        SoundHelper.reciveTip()
        return inflater.inflate(R.layout.fragment_give_back, container, false).apply {
            BraceletMachineManager.giveBackBracelet(this@GiveBackFragment)
        }
    }

    override fun onBack() {
        BraceletMachineManager.stopGiveBack()
    }

    override fun onGiveBackSuccess(no: String, hexNo: String) {
        showToast("$no\n$hexNo")
        SoundHelper.reciveSuccess()
        popFragment()
    }

    override fun onGiveBackFail(msg: String) {
        context?.let { ResultTipsDialog.Builder(it, ResultTipsDialog.Builder.State.ERROR, msg).create().show{
            popFragment()
        } }
    }

    override fun onCountDown(countDown: Int) {
        view?.apply {
            tvCountDown.visibility = View.VISIBLE
            tvCountDown.text = countDown.toString()
        }
    }
}
