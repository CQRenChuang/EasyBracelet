package com.ren_chaung.easy_bracelet.view.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.comocm.base.extension.showToast
import com.comocm.base.extension.startNewActivity
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletMachineSystemListener
import com.ocm.bracelet_machine_sdk.BraceletManager2
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.AppHelper
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.dialog.SetupNumDialog
import com.ren_chaung.easy_bracelet.view.dialog.SetupTwoNumDialog
import kotlinx.android.synthetic.main.activity_base_fragment.ivBack
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseFragmentActivity() {

    private var isOpenBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        ivBack.visibility = View.VISIBLE
        title = "手环机设置"
        val spinnerItems = arrayOf("IC", "ID")
        val hostAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
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
                AlertDialog.Builder(this@SettingActivity).apply {
                    setTitle("提示")
                    setMessage("需要重启才能配置生效")
                    setPositiveButton("立即重启"
                    ) { dialog, which ->
                        BraceletMachineManager.setCardType(if(position == 0) BraceletMachineManager.CardType.IC else BraceletMachineManager.CardType.ID)
                        AppHelper.restartApp(context)
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

        val spinnerFetchNumItems = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val fetchNumAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerFetchNumItems)
        fetchNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFetchNum.adapter = fetchNumAdapter
        spinnerFetchNum.setSelection(BraceletMachineManager.fetchNum-1)
        spinnerFetchNum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val oldPosition = BraceletMachineManager.fetchNum-1
                if (oldPosition == position) return
                BraceletMachineManager.setFetchNum(position+1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }


        val spinnerDeviceItems = arrayOf("单台收发", "两台发放")
        val deviceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerDeviceItems)
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceType.adapter = deviceAdapter
        val oldPosition = BraceletMachineManager.DeviceType.values().indexOf(BraceletMachineManager.deviceType)
        spinnerDeviceType.setSelection(oldPosition)
        spinnerDeviceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                BraceletMachineManager.setDeviceType(BraceletMachineManager.DeviceType.values()[position])
                refreshLayout()
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

        // 收发手环是否计数
        checkboxCalc.isChecked = BraceletMachineManager.enableCalc
        checkboxCalc.setOnCheckedChangeListener { buttonView, isChecked ->
            BraceletMachineManager.setEnableCalc(isChecked)
        }

        checkboxAutoRun.isChecked = BraceletMachineManager.enableAutoRun
        checkboxAutoRun.setOnCheckedChangeListener { buttonView, isChecked ->
            BraceletMachineManager.setEnableAutoRun(isChecked)
        }

        checkboxCheckSelfNo1.isChecked = BraceletMachineManager.checkSelfNo1
        checkboxCheckSelfNo2.isChecked = BraceletMachineManager.checkSelfNo2

        checkboxCheckSelfNo1.setOnCheckedChangeListener { buttonView, isChecked ->
            BraceletMachineManager.setCheckSelfNo1(isChecked)
        }
        checkboxCheckSelfNo2.setOnCheckedChangeListener { buttonView, isChecked ->
            BraceletMachineManager.setCheckSelfNo2(isChecked)
        }

        buttonSetNum.setOnSingleClickListener {
            when(BraceletMachineManager.deviceType) {
                BraceletMachineManager.DeviceType.Normal -> {
                    SetupNumDialog.create(this).show()
                }
                BraceletMachineManager.DeviceType.TwoFetch -> {
                    SetupTwoNumDialog.create(this).show()
                }
            }
        }

        buttonOpenBack.setOnSingleClickListener {
            if (isOpenBack) {
                BraceletMachineManager.sysStopPush(object : BraceletMachineSystemListener {
                    override fun onSuccess() {
                        buttonOpenBack.text = "打开回收口"
                        isOpenBack = !isOpenBack
                    }

                    override fun onFail() {
                        showToast("关闭失败")
                    }
                })

            } else {
                BraceletMachineManager.sysStartPush(object : BraceletMachineSystemListener {
                    override fun onSuccess() {
                        buttonOpenBack.text = "关闭回收口"
                        isOpenBack = !isOpenBack
                    }

                    override fun onFail() {
                        showToast("打开失败")
                    }
                })
            }
        }
        refreshLayout()
    }

    override fun backAction() {
        BraceletMachineManager.processDone()
        BraceletManager2.processDone()
        startNewActivity(MainActivity::class.java)
        finish()
    }

    private fun refreshLayout() {
        when(BraceletMachineManager.deviceType) {
            BraceletMachineManager.DeviceType.Normal -> {
                layoutCheckSelf.visibility = View.GONE
                buttonOpenBack.visibility = View.VISIBLE
            }
            BraceletMachineManager.DeviceType.TwoFetch -> {
                buttonOpenBack.visibility = View.GONE
                layoutCheckSelf.visibility = View.VISIBLE
            }
        }
    }
}