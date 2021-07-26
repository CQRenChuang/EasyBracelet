package com.ren_chaung.easy_bracelet.view.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.*
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import com.ren_chaung.easy_bracelet.view.dialog.ResultTipsDialog
import kotlinx.android.synthetic.main.fragment_give_back.view.*
import java.util.*
import kotlin.concurrent.timer

/**
 * 基础Fragment，都加上手环机回调监听
 */
class GiveBackFragment : BaseFragment(), WRGiveBackCallback {
    private var canGoBack = false
    private var timer: Timer? = null
    private var countDown = 10
    private var isStop = false
    private var retryTimer: Timer? = null
    private val handler = Handler()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setTitle("还手环")
        SoundHelper.reciveTip()
//        (activity as? BaseFragmentActivity)?.findViewById<View>(R.id.ivBack)?.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_give_back, container, false).apply {
            BraceletMachineManager.readRecyclingNo(this@GiveBackFragment)
            tvCountDown.text = countDown.toString()
            timer = timer(initialDelay = 1000, period = 1000) {
                countDown -= 1
                handler.post {
                    if (countDown < 0) {
                        timer?.cancel()
                        timer = null
                        onGiveBackFail("归还超时")
                    } else {
                        tvCountDown.text = countDown.toString()
                    }
                }
            }
        }
    }

    override fun canGoBack(): Boolean {
        if (!canGoBack) {
            showToast("操作中断中，即将返回")
            isStop = true
        } else {
            retryTimer?.cancel()
            retryTimer = null
            timer?.cancel()
            timer = null
            BraceletMachineManager.destoryGiveBack()
            BraceletMachineManager.stopGiveBack()
        }
        return canGoBack
    }

    override fun onBack() {
        timer?.cancel()
        timer = null
        retryTimer?.cancel()
        retryTimer = null
        canGoBack = true
        BraceletMachineManager.destoryGiveBack()
        BraceletMachineManager.stopGiveBack()
    }

    override fun onSuccess(card: CardDataModel) {
        if (countDown <= 0) { return }
        BraceletMachineManager.destoryGiveBack()
        BraceletMachineManager.openRecycling(object : AllowGiveBackCallback {
            override fun onSuccess() {
                showToast("${card.CardNo}\n${card.cardNoHex}")
                SoundHelper.reciveSuccess()
                onStopBack()
            }

            override fun onFail(msg: String) {
                this@GiveBackFragment.onGiveBackFail(msg)
            }
        })
    }

    fun onStopBack() {
        timer?.cancel()
        timer = null
        retryTimer?.cancel()
        retryTimer = null
        canGoBack = true
        popFragment()
    }

    override fun onGiveBackFail(msg: String) {
        if (msg.contains("未读取到数据")) {
            if (isStop) {
                onStopBack()
                return
            }
            if (countDown <= 0) { return }
            retryTimer = timer(initialDelay = 1000, period = 1000) {
                retryTimer?.cancel()
                retryTimer = null
                handler.post {
                    BraceletMachineManager.readRecyclingNo(this@GiveBackFragment)
                }
            }
            return
        }
        context?.let { ResultTipsDialog.Builder(it, ResultTipsDialog.Builder.State.ERROR, msg).create().show{
            onStopBack()
        } }
    }
}
