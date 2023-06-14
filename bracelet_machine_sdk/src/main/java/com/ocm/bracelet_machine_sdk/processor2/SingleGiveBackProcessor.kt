package com.ocm.bracelet_machine_sdk.processor2

import android.os.Handler
import android.util.Log
import com.ocm.bracelet_machine_sdk.AllowGiveBackCallback
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletManager2
import com.ocm.bracelet_machine_sdk.BraceletManager2.serialPortHelper
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.ocm.bracelet_machine_sdk.Machine.RobotData
import com.ocm.bracelet_machine_sdk.Machine.RobotMsg
import com.ocm.bracelet_machine_sdk.WRGiveBackCallback
import com.ocm.bracelet_machine_sdk.NumberManager2
import com.ocm.bracelet_machine_sdk.utils.LocalLogger

/**
 * 单步归还操作
 * @property handler Handler
 * @property callback AllowGiveBackCallback?
 * @property listener WRGiveBackCallback?
 */
internal class SingleGiveBackProcessor: BaseProcessor() {
    private val handler = Handler()
    private var callback: AllowGiveBackCallback? = null
    private var listener: WRGiveBackCallback? = null
//    private val backOpt = OptModel()

    fun start() {
    }

    fun readNo(listener: WRGiveBackCallback) {
        LocalLogger.write("归还开始")
        serialPortHelper?.setReciveNotify(true)
        this.listener = listener
        serialPortHelper?.SendCmd(
            RobotData.HOST.RECIVE,
            "01", 0)
    }

    fun readInfo(sector: BraceletMachineManager.SectorType, pwd: String, listener: WRGiveBackCallback) {
        serialPortHelper?.setReciveNotify(true)
        this.listener = listener
        serialPortHelper?.SendCmd(RobotData.HOST.RECIVE, "02${sector.cmd}${pwd}", 0)
    }

    fun readNoAndWriteSector(sector: BraceletMachineManager.SectorType, pwd: String, content: String, listener: WRGiveBackCallback) {
        serialPortHelper?.setReciveNotify(true)
        this.listener = listener
        serialPortHelper?.SendCmd(RobotData.HOST.RECIVE, "03${sector.cmd}${pwd}${content}", 0)
    }

    fun giveback(callback: AllowGiveBackCallback) {
        serialPortHelper?.setReciveNotify(true)
        this.callback = callback
        serialPortHelper?.SendCmd(RobotData.HOST.RECIVENABLE, "", 0)
    }

    /**
     * 主动停止还手环
     */
    fun stop() {
        LocalLogger.write("停止还手环")
        BraceletManager2.processDone()
        listener?.onCompleted()
    }

    fun destory() {
        callback = null
        listener = null
    }

    override fun OnMsg(msg: RobotMsg?, data: Any?) {
        super.OnMsg(msg, data)
        if (msg == null) {
            BraceletManager2.processDone()
            handler.post {
                listener?.onGiveBackFail("未读取到数据")
                listener?.onCompleted()
                callback?.onFail("未读取到数据")
                callback?.onCompleted()
            }
            return
        }
        when(msg) {
            RobotMsg.ReciveWait -> {
                handler.post {
                    BraceletManager2.processDone()
                    if (data is CardDataModel) {
                        listener?.onSuccess(data)
                        Log.e("data", data.toString())
                    } else {
                        listener?.onGiveBackFail("数据类型有误")
                    }
                    listener?.onCompleted()
                }
            }
            RobotMsg.ReciveSuccess -> {
                serialPortHelper?.setReciveNotify(false)
                BraceletManager2.processDone()
                NumberManager2.addCurrentNum(0)
                callback?.onSuccess()
                callback?.onCompleted()
                stop()
            }
            RobotMsg.ReciveSendRoll -> {
            }
            RobotMsg.ReciveFail -> {
                serialPortHelper?.setReciveNotify(false)
                handler.post {
                    BraceletManager2.processDone()
                    listener?.onGiveBackFail("归还失败")
                    listener?.onCompleted()
                    callback?.onFail("归还失败")
                    callback?.onCompleted()
                }
                stop()
            }
            else -> return
        }
    }
}