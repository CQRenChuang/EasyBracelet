package com.ren_chaung.easy_bracelet.utils

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Handler
import android.util.Log
import com.example.zh_idreader.IDReader
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.utils.StringHelper
import com.ocm.ocmlogger.OCMLogger
import kotlin.collections.ArrayList

object NFCHelper {

    private const val TAG = "NFCHelper"
    private val rfidCrl = IDReader()
    var listener: NFCListener? = null
    private val handler = Handler()

    fun decode(intent: Intent) {
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tagFromIntent?.let {
            var cardno = convertCardNo(StringHelper.bytesToHexFun3(it.id))
            cardno = java.lang.Long.parseLong(cardno, 16).toString()
            while (cardno.length < 10) cardno = "0$cardno"

            try {
                if (BraceletMachineManager.enableNFCFetch) {
                    val blockStr = decodeSector(tagFromIntent)
                    listener?.onNFCReadSuccess(cardno, blockStr)
                    return
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            listener?.onNFCReadSuccess(cardno, null)
        }
    }

    private fun convertCardNo(tmp: String?): String {
        var tmp = tmp
        if (tmp == null) tmp = "00000000"
        while (tmp!!.length < 8) tmp = "0$tmp"
        val cardChar = CharArray(8)
        try {
            cardChar[0] = tmp[6]
            cardChar[1] = tmp[7]
            cardChar[2] = tmp[4]
            cardChar[3] = tmp[5]
            cardChar[4] = tmp[2]
            cardChar[5] = tmp[3]
            cardChar[6] = tmp[0]
            cardChar[7] = tmp[1]
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return String(cardChar)
    }

    /**
     * 黄龙呼啦网络读卡方法
     * @param tag Tag
     */
    private fun decodeSector(tag: Tag): String {
//        val mc = MifareClassic.get(tag)
//        try {
//            mc.connect()
//            val pwd = StringHelper.hexStringToBytes(EquipmentManager.appConfig?.nfc_reader?.psw ?: "")
//            val auth = mc.authenticateSectorWithKeyA(EquipmentManager.appConfig?.nfc_reader?.sector ?: 1, pwd)
//            if (!auth) Throwable("验证失败")
//            try {
//                val datas = mc.readBlock(EquipmentManager.appConfig?.nfc_reader?.block ?: 1)
//                val blockStr = StringHelper.bytesToHexFun3(datas)
//                Log.e(TAG, "块： ${EquipmentManager.appConfig?.nfc_reader?.block}， 块内容：$blockStr")
//                return blockStr
//            } catch (ex: Exception) {
//                OCMLogger.write("读扇区失败 $ex，块： ${EquipmentManager.appConfig?.nfc_reader?.block}")
//            }
//        } catch (ex: Exception) {
//            Log.e(TAG, "操作异常 $ex")
//        } finally {
//            mc.close()
//        }
        return ""
    }

    fun closeZHRFID() {
        rfidCrl.closeRFID()
        rfidCrl.Sector_read_disable()
        mThreadRunning = false
        mRunning = false
    }

    private var mThreadRunning = true
    private var mRunning = false
    private val ActivityArray0 = IntArray(32)
    fun openZHRFID() {
        rfidCrl.openRFID()
        if (BraceletMachineManager.enableNFCFetch) {
            rfidCrl.Sector_read_enable()
            val pwd = StringHelper.hexStringToBytes("FFFFFFFFFFFF")
            val keyAndMode = ArrayList<Int>()
            keyAndMode.add(0x55)
            keyAndMode.add(0x0A)
            keyAndMode.addAll(pwd.map { it.toInt() })
            rfidCrl.Sector_set_key_and_mode(keyAndMode.toIntArray())

            val secNumAndBlockNum = ArrayList<Int>()
            secNumAndBlockNum.add(0x66)
            val sec = 2
            secNumAndBlockNum.add(sec)
            secNumAndBlockNum.add(2 - sec*4)
            rfidCrl.Sector_set_read_sectorNum_and_blockNum(secNumAndBlockNum.toIntArray())
        }
        mThreadRunning = true
        mRunning = true

        object : Thread() {
            override fun run() {
                super.run()
                Log.e("dxx", "thread run()")
                while (mThreadRunning) {
                    try {
                        sleep(200)
                    } catch (e: InterruptedException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    Log.e("dxx", "thread run(1)mRunning:$mRunning")
                    if (mRunning) {
                        rfidCrl.Read_rfid_data(ActivityArray0)
                        val cardNoArray = ActivityArray0.copyOfRange(4, 8)
                        var cardNo10D =
                            convertCardNo(StringHelper.bytesToHexFun3(cardNoArray.map { it.toByte() }
                                .toByteArray()))
                        cardNo10D = java.lang.Long.parseLong(cardNo10D, 16).toString()
                        while (cardNo10D.length < 10) cardNo10D = "0$cardNo10D"
                        Log.d("dxx", "cardNo10D: $cardNo10D")
                        Log.d("dxx",
                            "read: ${StringHelper.bytesToHexFun3(ActivityArray0.map { it.toByte() }
                                .toByteArray())}"
                        )
                        if (ActivityArray0[8] == 0) {
                            handler.post {
                                listener?.onNFCReadSuccess(cardNo10D, "")
                            }
                            continue
                        }
                        val blockArray = ActivityArray0.copyOfRange(9, 25)
                        val blockContent =
                            StringHelper.bytesToHexFun3(blockArray.map { it.toByte() }
                                .toByteArray())
                        Log.d("dxx", "blockContent: $blockContent")
                        handler.post {
                            listener?.onNFCReadSuccess(cardNo10D, blockContent)
                        }
                    }
                }
            }
        }.start()
    }

    interface NFCListener {
        fun onNFCReadSuccess(cardNo10D: String, blockContent: String?)
    }
}