package com.ren_chaung.easy_bracelet.view.activity

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.KeyEvent
import com.comocm.base.extension.showToast
import com.comocm.sound.SoundHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.ocmlogger.OCMLogger
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.NFCHelper
import com.ren_chaung.easy_bracelet.utils.extension.setOnMultiClickListener
import com.ren_chaung.easy_bracelet.view.dialog.SetupNumDialog
import com.ren_chaung.easy_bracelet.view.fragment.FetchFragment
import com.ren_chaung.easy_bracelet.view.fragment.InitFragment
import com.ren_chaung.easy_bracelet.view.fragment.MainFragment
import com.ren_chaung.easy_bracelet.view.fragment.SettingFragment
import com.tencent.bugly.beta.Beta
import floatwindow.xishuang.float_lib.FloatLoger
import kotlinx.android.synthetic.main.activity_base_fragment.*
import kotlinx.android.synthetic.main.activity_main.*

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
        replaceFragment(InitFragment(object : InitFragment.InitFragmentListener {
            override fun initSuccess() {
                isInitSuccess = true
                replaceFragment(MainFragment(), FragmentAnimateType.FADE)
                openNFC()
            }
        }), FragmentAnimateType.FADE)

        tvVersion.setOnMultiClickListener(3) {
            if (!isInitSuccess || fragments.size > 1) {
                showToast("开始检查更新")
                Beta.checkUpgrade()
                return@setOnMultiClickListener
            }
            push(SettingFragment())
        }

        layoutNumber.setOnMultiClickListener(5) {
            SetupNumDialog.create(this).show()
        }

        tvNumber.text = BraceletMachineManager.currentBracelet.toString()
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener {
            override fun onCurrentNumChange(num: Int) {
                tvNumber.text = num.toString()
            }
        })

        //NFC
        NFCHelper.closeZHRFID()
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        mAdapter?.disableForegroundDispatch(this)
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
                if (BraceletMachineManager.enableNFCFetch && fragments.size == 1)
                    push(FetchFragment())
            }
        }
        NFCHelper.openZHRFID()
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
        if ((keyValue < 32 || keyValue > 126)) { return false }
        qrCode = "$qrCode${keyValue.toChar()}"
        return false
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
        push(FetchFragment())
    }
}