package com.ren_chaung.easy_bracelet.view.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.comocm.base.extension.showToast
import com.comocm.base.extension.startNewActivity
import com.ocm.bracelet_machine_sdk.BraceletListener
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletManager2
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.NFCHelper
import com.ren_chaung.easy_bracelet.utils.extension.setOnMultiClickListener
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.dialog.SetupNumDialog
import com.ren_chaung.easy_bracelet.view.dialog.SetupTwoNumDialog
import com.ren_chaung.easy_bracelet.view.fragment.FetchFragment
import com.ren_chaung.easy_bracelet.view.fragment.FetchFragment2
import com.ren_chaung.easy_bracelet.view.fragment.InitFragment
import com.ren_chaung.easy_bracelet.view.fragment.MainFragment
import com.ren_chaung.easy_bracelet.view.fragment.MainFragment2
import floatwindow.xishuang.float_lib.FloatLoger
import kotlinx.android.synthetic.main.activity_base_fragment.ivNavRight
import kotlinx.android.synthetic.main.activity_base_fragment.tvVersion
import kotlinx.android.synthetic.main.activity_main.layoutMainContainer
import kotlinx.android.synthetic.main.activity_main.layoutNumber
import kotlinx.android.synthetic.main.activity_main.layoutNumber1
import kotlinx.android.synthetic.main.activity_main.layoutNumber2
import kotlinx.android.synthetic.main.activity_main.layoutTwoFetchNumber
import kotlinx.android.synthetic.main.activity_main.tvNumber
import kotlinx.android.synthetic.main.activity_main.tvNumber1
import kotlinx.android.synthetic.main.activity_main.tvNumber2

class MainActivity : BaseFragmentActivity() {
    private var isInitSuccess = false

    //NFC
    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    private var qrCode = ""
    private var toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragmentContainer(layoutMainContainer.id)
        title = getString(R.string.app_name)
        replaceFragment(InitFragment.newInstance(object : InitFragment.InitFragmentListener {
            override fun initSuccess() {
                isInitSuccess = true
                when (BraceletMachineManager.deviceType) {
                    BraceletMachineManager.DeviceType.Normal -> {
                        replaceFragment(MainFragment(), FragmentAnimateType.FADE)
                    }

                    BraceletMachineManager.DeviceType.TwoFetch -> {
                        replaceFragment(MainFragment2(), FragmentAnimateType.FADE)
                    }
                }
                openNFC()
            }
        }), FragmentAnimateType.FADE)

        ivNavRight.visibility = View.VISIBLE
        ivNavRight.setOnSingleClickListener {
            startNewActivity(SettingActivity::class.java)
            finish()
        }

        tvVersion.setOnMultiClickListener(3) {
            showToast("无更新")
        }

        layoutNumber.setOnMultiClickListener(5) {
            setupNum()
        }
        layoutNumber2.setOnMultiClickListener(5) {
            setupNum()
        }
        layoutNumber1.setOnMultiClickListener(5) {
            setupNum()
        }

        tvNumber.text = BraceletMachineManager.currentBracelet.toString()
        tvNumber1.text = BraceletManager2.getCurrentBracelet(0).toString()
        tvNumber2.text = BraceletManager2.getCurrentBracelet(1).toString()
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener {
            override fun onCurrentNumChange(num: Int) {
                tvNumber.text = num.toString()
            }
        })
        BraceletManager2.setBraceletMachineListener(object : BraceletListener {
            override fun onCurrentNumChange(addr: Int, num: Int) {
                if (addr == 0) {
                    tvNumber1.text = num.toString()
                } else {
                    tvNumber2.text = num.toString()
                }
            }
        })

        //NFC
        NFCHelper.closeZHRFID()
    }

    private fun setupNum() {
        when (BraceletMachineManager.deviceType) {
            BraceletMachineManager.DeviceType.Normal -> {
                SetupNumDialog.create(this).show()
            }

            BraceletMachineManager.DeviceType.TwoFetch -> {
                SetupTwoNumDialog.create(this).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        mAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
        when (BraceletMachineManager.deviceType) {
            BraceletMachineManager.DeviceType.Normal -> {
                layoutNumber.visibility = View.VISIBLE
                layoutTwoFetchNumber.visibility = View.GONE
            }

            BraceletMachineManager.DeviceType.TwoFetch -> {
                layoutNumber.visibility = View.GONE
                layoutTwoFetchNumber.visibility = View.VISIBLE
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        mAdapter?.disableForegroundDispatch(this)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BraceletMachineManager.enableNFCFetch && fragments.size == 1 && isInitSuccess) {
            NFCHelper.decode(intent)
        }
    }

    override fun onDestroy() {
        try {
            FloatLoger.getInstance().stopServer(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            NFCHelper.closeZHRFID()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun openNFC() {
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        mPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        mAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
        NFCHelper.listener = object : NFCHelper.NFCListener {
            override fun onNFCReadSuccess(cardNo10D: String, blockContent: String?) {
                if (BraceletMachineManager.enableNFCFetch && fragments.size == 1) {
                    fetch()
                }
            }
        }
        NFCHelper.openZHRFID()
    }

    private fun fetch() {
        if (BraceletMachineManager.deviceType == BraceletMachineManager.DeviceType.Normal) {
            push(FetchFragment())
        } else {
            if (BraceletMachineManager.checkSelfNo1 && !BraceletMachineManager.checkSelfNo2) {
                push(FetchFragment2(0))
            }
            if (!BraceletMachineManager.checkSelfNo1 && BraceletMachineManager.checkSelfNo2) {
                push(FetchFragment2(1))
            }
            if (BraceletMachineManager.checkSelfNo1 && BraceletMachineManager.checkSelfNo2) {
                val index = (0..1).random()
                push(FetchFragment2(index))
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val keyValue = event?.unicodeChar ?: 0
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return false
        }
        if (!isInitSuccess || fragments.size > 1 || !BraceletMachineManager.enableQRFetch) {
            return false
        }
        if (keyValue == 10) {
            onScanQRCodeSuccess(qrCode)
            qrCode = ""
            return false
        }
        if ((keyValue < 32 || keyValue > 126)) {
            return false
        }
        qrCode = "$qrCode${keyValue.toChar()}"
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        //super.onSaveInstanceState(outState)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    //解码成功
    private fun onScanQRCodeSuccess(result: String?) {
        if (!BraceletMachineManager.enableQRFetch) {
            return
        }
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
        fetch()
    }
}