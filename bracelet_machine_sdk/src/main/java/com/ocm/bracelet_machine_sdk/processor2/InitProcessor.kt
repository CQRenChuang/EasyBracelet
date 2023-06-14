package com.ocm.bracelet_machine_sdk.processor2

import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import java.util.*

/**
 * 初始化业务处理
 */
internal class InitProcessor(private val addrIndex: Int): BaseProcessor() {

    private var inInit = false
    var initDone = false
    private var cardType = BraceletMachineManager.CardType.IC
    private var listener: com.ocm.bracelet_machine_sdk.CheckSelfCallback? = null

    fun start(cardType: BraceletMachineManager.CardType) {
//        if (inInit) { return }
        this.cardType = cardType
        inInit = true
        setCardType()
    }

    fun setCallback(listener: com.ocm.bracelet_machine_sdk.CheckSelfCallback) {
        this.listener = listener
    }

    /**
     * 设置卡片类型
     * @param cardType CardType
     */
    private fun setCardType() {
        LocalLogger.write("设置卡类型")
        BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.SETCARDTYPE, cardType.cmd, addrIndex)
    }

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        super.OnMsg(msg, data)
        when(msg) {
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.InitSuccess -> {
                initDone = true
                BraceletMachineManager.processDone()
                listener?.onCheckSelfSuccess()
                listener?.onCompleted()
            }
            com.ocm.bracelet_machine_sdk.Machine.RobotMsg.InitFail -> {
                BraceletMachineManager.processDone()
                listener?.onCheckSelfFail(data.toString())
                listener?.onCompleted()
            }
            else -> return
        }
    }


}