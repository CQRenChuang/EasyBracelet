package com.ocm.bracelet_machine_sdk.processor2

import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.serialPortHelper
import com.ocm.bracelet_machine_sdk.BraceletManager2
import java.util.*
import kotlin.concurrent.timer

/**
 * 初始化业务处理
 */
internal class TestProcessor: BaseProcessor() {


    private var timeoutTimer: Timer? = null
    private var listener: com.ocm.bracelet_machine_sdk.CheckStatusCallback? = null

    fun start() {
        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.TEST, "", 0)
        timeoutTimer?.cancel()
        timeoutTimer = timer(initialDelay = 5000, period = 5000){
            timeoutTimer?.cancel()
            timeoutTimer = null
            BraceletManager2.processDone()
            listener?.onFail("检测状态超时")
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
                BraceletManager2.processDone()
                listener?.onSuccess(BraceletMachineManager.BraceletMachineStatus.HAS_CACHE)
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.Fail -> {
                timeoutTimer?.cancel()
                timeoutTimer = null
                BraceletManager2.processDone()
                listener?.onSuccess(BraceletMachineManager.BraceletMachineStatus.NO_CACHE)
                listener?.onCompleted()
            }
            else -> return
        }
    }

}