package com.ocm.bracelet_machine_sdk.processor

import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import java.util.*
import kotlin.concurrent.timer

/**
 * 初始化业务处理
 */
internal class TestProcessor: BaseProcessor() {


    private var timeoutTimer: Timer? = null
    private var listener: com.ocm.bracelet_machine_sdk.CheckStatusCallback? = null

    fun start() {
        BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.TEST, "")
        timeoutTimer?.cancel()
        timeoutTimer = timer(initialDelay = 5000, period = 5000){
            timeoutTimer?.cancel()
            timeoutTimer = null
            listener?.onFail("检测状态超时")
            BraceletMachineManager.processDone()
            listener?.onCompleted()
        }
    }

    fun setCallback(listener: com.ocm.bracelet_machine_sdk.CheckStatusCallback) {
        this.listener = listener
    }

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        super.OnMsg(msg, data)
        when(msg) {
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.Success -> {
                timeoutTimer?.cancel()
                timeoutTimer = null
                listener?.onSuccess(BraceletMachineManager.BraceletMachineStatus.HAS_CACHE)
                BraceletMachineManager.processDone()
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.Fail -> {
                timeoutTimer?.cancel()
                timeoutTimer = null
                listener?.onSuccess(BraceletMachineManager.BraceletMachineStatus.NO_CACHE)
                BraceletMachineManager.processDone()
                listener?.onCompleted()
            }
            else -> return
        }
    }

}