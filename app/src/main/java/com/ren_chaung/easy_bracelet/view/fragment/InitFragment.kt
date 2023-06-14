package com.ren_chaung.easy_bracelet.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletManager2
import com.ocm.bracelet_machine_sdk.CheckSelfCallback
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import kotlinx.android.synthetic.main.fragment_init.view.*
import java.io.Serializable

/**
 * 基础Fragment，都加上手环机回调监听
 */
class InitFragment : BaseFragment() {


    private var listener: InitFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listener = it.getSerializable(ARG_PARAM1) as? InitFragmentListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_init, container, false).apply {
            tvState.text = "开始自检"
            when (BraceletMachineManager.deviceType) {
                BraceletMachineManager.DeviceType.Normal -> {
                    BraceletMachineManager.checkSelf(object : CheckSelfCallback {
                        override fun onCheckSelfFail(msg: String) {
                            tvState.text = msg
                        }

                        override fun onCheckSelfSuccess() {
                            tvState.text = "自检成功"
                            listener?.initSuccess()
                        }
                    })
                }

                BraceletMachineManager.DeviceType.TwoFetch -> {
                    if (BraceletMachineManager.checkSelfNo1) {
                        checkNo1()
                    } else if (BraceletMachineManager.checkSelfNo2) {
                        checkNo2()
                    } else {
                        tvState.text = "自检成功"
                        listener?.initSuccess()
                    }
                }
            }
        }
    }

    private fun checkNo1() {
        view?.tvState?.text = "自检手环机1"
        BraceletManager2.checkSelf(0, object : CheckSelfCallback {
            override fun onCheckSelfFail(msg: String) {
                view?.tvState?.text = msg
            }

            override fun onCheckSelfSuccess() {
                if (BraceletMachineManager.checkSelfNo2) {
                    checkNo2()
                } else {
                    view?.tvState?.text = "自检成功"
                    listener?.initSuccess()
                }
            }
        })
    }

    private fun checkNo2() {
        view?.tvState?.text = "自检手环机2"
        BraceletManager2.checkSelf(1, object : CheckSelfCallback {
            override fun onCheckSelfFail(msg: String) {
                view?.tvState?.text = msg
            }

            override fun onCheckSelfSuccess() {
                view?.tvState?.text = "自检成功"
                listener?.initSuccess()
            }
        })
    }

    interface InitFragmentListener : Serializable {
        fun initSuccess()
    }


    companion object {
        private const val ARG_PARAM1 = "param1"

        @JvmStatic
        fun newInstance(param1: InitFragmentListener) =
            InitFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}
