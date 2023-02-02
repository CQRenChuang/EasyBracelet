   package com.ocm.bracelet_machine_sdk.processor

import android.content.Context
import android.os.Handler
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.BraceletNumberManager
import com.ocm.bracelet_machine_sdk.R
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.timer

internal class FetchProcessor(context: Context) : BaseProcessor() {
    private var contextReference: WeakReference<Context> = WeakReference(context)
    private var getBrandAgainNumber = 0
    private val handler = Handler()
    private var listener: com.ocm.bracelet_machine_sdk.FetchCallback? = null
    var fetchCount = 0
    private var lastContent: String? = ""
    private var isStopFetch = false
    private var timer: Timer? = null
    private var isSending = false
    private var sendTimer: Timer? = null

    /**
     * 是否停止转动
     */
    var isStop = false

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        super.OnMsg(msg, data)
        if(BraceletMachineManager.machineState != BraceletMachineManager.MachineState.FETCHING) {
            return
        }
        isSending = false
        sendTimer?.cancel()
        sendTimer = null
        when(msg) {
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetSuccess -> {
                (data as? com.ocm.bracelet_machine_sdk.Machine.CardDataModel)?.let { card ->
                    getBrandAgainNumber = 0
                    BraceletNumberManager.desCurrentNum()
                    if (fetchCount > 1) {
                        listener?.onFetchSuccess(card.CardNo, card.cardNoHex)
                        fetchCount -= 1
                        listener?.onRemainingFetch(fetchCount)
                        fetch(lastContent)

                    } else {
                        BraceletMachineManager.processDone()
                        listener?.onFetchSuccess(card.CardNo, card.cardNoHex)
                        listener?.onCompleted()
                    }
                }
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetSendSuccess -> { }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetSendRoll -> {}
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetSendFail -> {
                BraceletMachineManager.processDone()
                listener?.onFetchFail(contextReference.get()?.getString(R.string.cmd_send_fail) ?: "")
                BraceletNumberManager.desCurrentNum()
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetFail -> {
                BraceletMachineManager.processDone()
                if (getBrandAgainNumber > 3) {
                    LocalLogger.write("连续5次取手环失败")
                    getBrandAgainNumber = 0
                    listener?.onFetchFail(
                        contextReference.get()?.getString(
                            R.string.error_robot) ?: "")
                    if (isStop) return
                    isStop = true
                    BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.STOP, "")
                    BraceletMachineManager.listener?.onStateChange(true)

                } else {
                    listener?.onFetchFail(
                        contextReference.get()?.getString(
                            R.string.error_brand) ?: "")
                }
                BraceletNumberManager.desCurrentNum()
                getBrandAgainNumber++
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.RobotStatusChange -> {
                if (isStop) return
                isStop = true
                BraceletMachineManager.processDone()
                listener?.onFetchFail("没有找到手环")
                BraceletMachineManager.listener?.onStateChange(true)
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.StartSuccess -> {
                if (isStopFetch) {
                    stopBack()
                    return
                }
                sendFetch()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.Busy -> {
                if (isStopFetch) {
                    stopBack()
                    return
                }
                timer?.cancel()
                timer = timer(initialDelay = 3000, period = 3000) {
                    if (isStop || isStopFetch) {
                        stopBack()
                        return@timer
                    }
                    fetch(lastContent)
                    timer?.cancel()
                    timer = null
                }
            }
            else -> return
        }
    }

    fun setCallback(listener: com.ocm.bracelet_machine_sdk.FetchCallback) {
        this.listener = listener
    }

    fun stop() {
        timer?.cancel()
        timer = null
        isStopFetch = true
        if (!isSending) {
            stopBack()
        }
    }

    private fun stopBack() {
        handler.post {
            sendTimer?.cancel()
            sendTimer = null
            isStopFetch = false
            listener?.onStopBack()
            BraceletMachineManager.processDone()
            listener?.onCompleted()
        }
    }

    fun destory() {
        timer?.cancel()
        timer = null
        sendTimer?.cancel()
        sendTimer = null
    }

    fun fetch(content: String? = null) {
        isStopFetch = false
        LocalLogger.write("获取手环 , fetchCount： $fetchCount")
        lastContent = content
        sendFetch()
    }

    private fun sendFetch() {
        try {
            isSending = true
            sendTimer = timer(initialDelay = 25000, period = 25000) {
                cancel()
                sendTimer?.cancel()
                sendTimer = null
                timer?.cancel()
                timer = null
                isStopFetch = true
                handler.post {
                    listener?.onReceiveTimeout()    
                    BraceletMachineManager.processDone()
                    listener?.onCompleted()                
                }
            }
            if (isStop) {
                isStop = false
                BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.START, "")
                BraceletMachineManager.listener?.onStateChange(false)
            } else {
                BraceletMachineManager.serialPortHelper?.SendCmd(
                    com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.TAKEBRAND, lastContent ?: "")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
