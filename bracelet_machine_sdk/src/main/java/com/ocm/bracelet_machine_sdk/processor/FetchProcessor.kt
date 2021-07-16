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
    private var reSendCount = 0

    /**
     * 是否停止转动
     */
    var isStop = false

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        isSending = false
        sendTimer?.cancel()
        sendTimer = null
        when(msg) {
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.GetSuccess -> {
                (data as? com.ocm.bracelet_machine_sdk.Machine.CardDataModel)?.let { card ->
                    getBrandAgainNumber = 0
                    BraceletMachineManager.processDone()
                    listener?.onFetchSuccess(card.CardNo, card.cardNoHex)
                    BraceletNumberManager.desCurrentNum()
                    if (fetchCount > 1) {
                        fetchCount -= 1
                        listener?.onRemainingFetch(fetchCount)
                        fetch(lastContent)

                    } else {
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
                    isStopFetch = false
                    listener?.onStopBack()
                    return
                }
                sendFetch()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.Busy -> {
                if (isStopFetch) {
                    isStopFetch = false
                    listener?.onStopBack()
                    return
                }
                timer = timer(initialDelay = 3000, period = 3000) {
                    if (isStop || isStopFetch) {
                        isStopFetch = false
                        return@timer
                    }
                    sendFetch()
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
        LocalLogger.write("调用stop停止取手环")
        if (!isSending) {
            listener?.onStopBack()
        }
        BraceletMachineManager.processDone()
        listener?.onCompleted()
    }

    fun destory() {
        timer?.cancel()
        timer = null
        sendTimer?.cancel()
        sendTimer = null
        reSendCount = 0
    }

    fun fetch(content: String? = null) {
        if (isStop) {
            isStop = false
            BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.START, "")
            BraceletMachineManager.listener?.onStateChange(false)
        }
        isStopFetch = false
        LocalLogger.write("获取手环 , fetchCount： $fetchCount")
        lastContent = content
        sendFetch()
    }

    private fun sendFetch() {
        try {
            isSending = true
            sendTimer = timer(initialDelay = 5000, period = 5000) {
                LocalLogger.write("获取手环没有响应，尝试重发： $reSendCount")
                sendTimer?.cancel()
                sendTimer = null
                if(reSendCount > 3) {
                    reSendCount = 0
                    handler.post {
                        BraceletMachineManager.processDone()
                        listener?.onFetchFail("没有收到响应")
                        listener?.onCompleted()
                    }
                    return@timer
                }
                reSendCount += 1
                sendFetch()
            }
            if (isStop) {
                isStop = false
                BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.START, "")
            } else {
                BraceletMachineManager.serialPortHelper?.SendCmd(
                    com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.TAKEBRAND, lastContent ?: "")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}