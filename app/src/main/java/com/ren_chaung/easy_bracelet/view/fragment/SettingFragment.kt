package com.ren_chaung.easy_bracelet.view.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.comocm.base.extension.showToast
import com.ocm.bracelet_machine_sdk.AllowGiveBackCallback
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletMachineSystemListener
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.AppHelper
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.activity.MainActivity
import com.ren_chaung.easy_bracelet.view.dialog.SetupNumDialog
import kotlinx.android.synthetic.main.fragment_setting.view.*


/**
 * 基础Fragment，都加上手环机回调监听
 */
class SettingFragment : BaseFragment() {

    private var isOpenBack = false


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setTitle("手环机设置")
        return inflater.inflate(R.layout.fragment_setting, container, false).apply {
            val spinnerItems = arrayOf("IC", "ID")
            val hostAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, spinnerItems)
            hostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCardType.adapter = hostAdapter
            spinnerCardType.setSelection(if(BraceletMachineManager.isIC()) 0 else 1)
            spinnerCardType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val oldPosition = if(BraceletMachineManager.isIC()) 0 else 1
                    if (oldPosition == position) return
                    AlertDialog.Builder(context).apply {
                        setTitle("提示")
                        setMessage("需要重启才能配置生效")
                        setPositiveButton("立即重启"
                        ) { dialog, which ->
                            BraceletMachineManager.setCardType(if(position == 0) BraceletMachineManager.CardType.IC else BraceletMachineManager.CardType.ID)
                            AppHelper.restartApp(context, MainActivity::class.java)
                        }
                        setNegativeButton("取消设置") { dialog, which ->
                            spinnerCardType.setSelection(if(BraceletMachineManager.isIC()) 0 else 1)
                        }

                    }.create().apply {
                        setCanceledOnTouchOutside(false)
                    }.show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }

            checkboxQR.isChecked = BraceletMachineManager.enableQRFetch
            checkboxQR.setOnCheckedChangeListener { buttonView, isChecked ->
                BraceletMachineManager.setEnableQRFetch(isChecked)
            }

            checkboxNFC.isChecked = BraceletMachineManager.enableNFCFetch
            checkboxNFC.setOnCheckedChangeListener { buttonView, isChecked ->
                BraceletMachineManager.setEnableNFCFetch(isChecked)
            }

            refreshData(this)
            buttonSetNum.setOnSingleClickListener {
                SetupNumDialog.create(context).show()
            }

            openBack.setOnSingleClickListener {
                if (isOpenBack) {
                    BraceletMachineManager.sysStartPush(object : BraceletMachineSystemListener {
                        override fun onSuccess() {
                            openBack.text = "打开回收口"
                            isOpenBack = !isOpenBack
                        }

                        override fun onFail() {
                            context.showToast("打开失败")
                        }
                    })

                } else {
                    BraceletMachineManager.sysStopPush(object : BraceletMachineSystemListener {
                        override fun onSuccess() {
                            openBack.text = "关闭回收口"
                            isOpenBack = !isOpenBack
                        }

                        override fun onFail() {
                            context.showToast("关闭失败")
                        }
                    })
                }
            }
        }
    }

    private fun refreshData(view: View?) {
        view?.apply {
            buttonSetNum.text = "数量配置（Max:${BraceletMachineManager.maxBracelet})"
        }
    }

    override fun onBack() {
        super.onBack()
        if (isOpenBack) {
            BraceletMachineManager.sysStopPush(object : BraceletMachineSystemListener {
                override fun onSuccess() {
                }

                override fun onFail() {
                }
            })
        }
    }
}
