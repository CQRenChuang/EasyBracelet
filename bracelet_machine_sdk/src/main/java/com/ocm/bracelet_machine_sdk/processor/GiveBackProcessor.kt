package com.ocm.bracelet_machine_sdk.processor

import android.os.Handler
import com.ocm.bracelet_machine_sdk.*
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.serialPortHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.stopGiveBack
import com.ocm.bracelet_machine_sdk.BraceletNumberManager
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.ocm.bracelet_machine_sdk.Machine.OptModel
import com.ocm.bracelet_machine_sdk.Machine.RobotData
import com.ocm.bracelet_machine_sdk.Machine.RobotMsg
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import java.util.*

internal class GiveBackProcessor: BaseProcessor() {
    //定时器处理
    private var giveBackTimer: Timer? = null
    private var giveBackRollCount = 0
    private val handler = Handler()
    private var cardDataModel: CardDataModel? = null
    private var giveBackTimerCount = 0
    private var listener: com.ocm.bracelet_machine_sdk.GiveBackCallback? = null
    val backOpt = OptModel()
    var timeout = 6L //归还超时限定

    fun setCallback(callback: com.ocm.bracelet_machine_sdk.GiveBackCallback) {
        listener = callback
    }

    fun start() {
        LocalLogger.write("归还开始")
        giveBackRollCount = 0
        giveBackTimerCount = 0
        giveBackTimer?.cancel()
        giveBackTimer = null
        cardDataModel = null
        serialPortHelper?.setReciveNotify(true)
        giveBackTimer = Timer()
        giveBackRollCount = 1
        giveBackTimer?.schedule(object : TimerTask(){
            override fun run() {
                if (cardDataModel != null) {
                    LocalLogger.write("已获得手环信息，等待验证")
                    return
                }
                if (giveBackTimerCount++ > 0) {
                    LocalLogger.write("归还手环超时")
                    //归还手环超时
                    serialPortHelper?.setReciveNotify(false)
                    handler.post {
                        listener?.onGiveBackFail("归还超时")
                    }
                    stop()
                    return
                }
                if (giveBackRollCount > 0) {
                    handler.post {
                        listener?.onGiveBackBusy()
                    }
                }
            }
        }, (timeout/2)*1000, (timeout/2)*1000)
        serialPortHelper?.SendCmd(
            RobotData.HOST.RECIVE,
            "01")
    }

    /**
     * 主动停止还手环
     */
    fun stop() {
        LocalLogger.write("停止还手环")
        giveBackRollCount = 0
        giveBackTimerCount = 0
        giveBackTimer?.cancel()
        giveBackTimer = null
        cardDataModel = null
        BraceletMachineManager.processDone()
        listener?.onCompleted()
    }

    override fun OnMsg(msg: RobotMsg?, data: Any?) {
        if (msg == null) {
            giveBackRollCount = 0
            if (cardDataModel != null) {
//                handler.postDelayed({
                    serialPortHelper?.simpleSendCmd(RobotData.HOST.RECIVE, "${backOpt.type}${backOpt.key}${backOpt.pwd}${backOpt.sectorContent}")
//                }, 500)
                return
            }
            if(giveBackTimer != null) {//等待归还手环
                handler.postDelayed({
                    serialPortHelper?.SendCmd(
                        RobotData.HOST.RECIVE,
                        "01")
                }, 500)
            }
            return
        }
        when(msg) {
            RobotMsg.ReciveWait -> {
                giveBackRollCount = 0
                (data as? CardDataModel)?.let { card ->
                    if (cardDataModel != null) {
                        serialPortHelper?.simpleSendCmd(RobotData.HOST.RECIVENABLE, "")
                        return@let
                    }
                    cardDataModel = card
                    listener?.let { listener ->
                        listener.checkAllowGiveBack(card, object : CheckGiveBackCallback {
                            override fun allow(checkResult: Boolean, msg: String) {
                                if(giveBackTimer == null) return
                                giveBackTimer?.cancel()
                                giveBackTimer = null
                                if (checkResult) {
                                    handler.postDelayed({
                                        if (backOpt.type == "03")
                                            serialPortHelper?.simpleSendCmd(RobotData.HOST.RECIVE, "${backOpt.type}${backOpt.key}${backOpt.pwd}${backOpt.sectorContent}")
                                        else {
                                            serialPortHelper?.simpleSendCmd(
                                                RobotData.HOST.RECIVENABLE,
                                                ""
                                            )
                                        }

                                    }, 500)
                                } else {
                                    handler.postDelayed({
                                        listener.onGiveBackFail(msg)
                                    }, 100)
                                    stop()
                                }
                            }
                        })
                    }
                }
            }
            RobotMsg.ReciveSuccess -> {
                serialPortHelper?.setReciveNotify(false)
                BraceletNumberManager.addCurrentNum()
                cardDataModel?.let { card ->
                    listener?.onGiveBackSuccess(card.CardNo)
                }
                stop()
            }
            RobotMsg.ReciveSendRoll -> {
                giveBackRollCount = 0
            }
            RobotMsg.ReciveFail -> {
                serialPortHelper?.setReciveNotify(false)
                handler.post {
                    listener?.onGiveBackFail("归还失败")
                }
                stop()
            }
            else -> return
        }
    }
}