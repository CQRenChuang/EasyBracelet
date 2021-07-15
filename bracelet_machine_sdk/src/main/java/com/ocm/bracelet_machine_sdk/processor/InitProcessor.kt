package com.ocm.bracelet_machine_sdk.processor

import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.model.EquipmentInfoBean
import com.ocm.bracelet_machine_sdk.model.WirteReadSet
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import java.util.*
import kotlin.math.min

/**
 * 初始化业务处理
 */
internal class InitProcessor: BaseProcessor() {


    private var timeoutTimer: Timer? = null
    private var inInit = false
    var initDone = false
    private var equipmentInfoBean: EquipmentInfoBean? = null
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
        BraceletMachineManager.serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.SETCARDTYPE, cardType.cmd)
    }

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
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


    /**
     * 根据商户设备信息配置机器
     * @param equipmentInfoBean EquipmentInfoBean
     */
    private fun config(equipmentInfoBean: EquipmentInfoBean) {
        this.equipmentInfoBean = equipmentInfoBean
        setOpt()
    }

    private fun setOpt() {
        equipmentInfoBean?.let { equipmentInfoBean ->
            try {
                val borrowWriteSet = equipmentInfoBean.json_config.borrow_set.write_set
                val borrowReadSet = equipmentInfoBean.json_config.borrow_set.read_set
                when {
                    borrowWriteSet.isNotEmpty() -> {
                        //借手环 写
                        try {
                            BraceletMachineManager.borrowOpt.key = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(borrowWriteSet[0].sector)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        BraceletMachineManager.borrowOpt.type = "03"
                        BraceletMachineManager.borrowOpt.pwd = borrowWriteSet[0].psw
                    }
                    borrowReadSet.isNotEmpty() -> {
                        //借手环 读
                        var block = 8
                        try {
                            BraceletMachineManager.borrowOpt.key = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(borrowReadSet[0].sector)
                            BraceletMachineManager.borrowOpt.block = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(borrowReadSet[0].block)
                            block = Integer.parseInt(borrowReadSet[0].block)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        BraceletMachineManager.borrowOpt.type = "02"
                        BraceletMachineManager.borrowOpt.pwd = borrowWriteSet[0].psw
                        BraceletMachineManager.serialPortHelper?.setBlock(block)
                    }
                    else -> BraceletMachineManager.borrowOpt.type = "01"
                }
                val backWriteSet = equipmentInfoBean.json_config.back_set.write_set
                val backReadSet = equipmentInfoBean.json_config.back_set.read_set
                when {
                    backWriteSet.isNotEmpty() -> {
                        //还手环 写
                        try {
                            BraceletMachineManager.backOpt.key = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(backWriteSet[0].sector)
                            BraceletMachineManager.backOpt.block = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(backWriteSet[0].block)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        BraceletMachineManager.backOpt.type = "03"
                        BraceletMachineManager.backOpt.pwd = backWriteSet[0].psw
                    }
                    backReadSet.isNotEmpty() -> {
                        //还手环 读
                        var block = 8
                        try {
                            BraceletMachineManager.backOpt.key = com.ocm.bracelet_machine_sdk.utils.StringHelper.str2HexStr(backReadSet[0].sector)
                            block = Integer.parseInt(backReadSet[0].block)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        BraceletMachineManager.backOpt.type = "02"
                        BraceletMachineManager.serialPortHelper?.setBlock(block)
                        BraceletMachineManager.backOpt.pwd = backReadSet[0].psw
                    }
                    else -> BraceletMachineManager.backOpt.type = "01"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                setContent()
            }
        }
    }

    private fun setContent() {
        equipmentInfoBean?.let { equipmentInfoBean ->
            if (BraceletMachineManager.borrowOpt.type == "03") {
                val content = analyContent(equipmentInfoBean.json_config.borrow_set.write_set)
                BraceletMachineManager.borrowOpt.optMap[BraceletMachineManager.borrowOpt.key] = content
            }
            if (BraceletMachineManager.backOpt.type == "03") {
                val content = analyContent(equipmentInfoBean.json_config.back_set.write_set)
                BraceletMachineManager.backOpt.optMap[BraceletMachineManager.backOpt.key] = content
            }
        }
    }


    private fun analyContent(wrSet: List<WirteReadSet>): String {
        val emptyBlock = "6F636D000000000000000000000000AB"
        val clearCup = "1"
        val clearOLD = "00000000000000000000000000000000"
        val clearNEW = "6F636D000000000000000000000000AB"
        val waterPump = "2"
        var content = ""
        val len = min(wrSet.size, 3)
        for (i in 0 until len) {
            val wr = wrSet[i]
            when(wr.opt_type) {
                clearCup -> content += if (wr.content == "0") clearOLD else clearNEW
                waterPump -> {
                    val params = wr.content.split(",").toTypedArray()
                    val paramsInt = intArrayOf(1, 1, 5)
                    try {
                        paramsInt[0] = Integer.parseInt(params[0])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        paramsInt[1] = Integer.parseInt(params[1])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        paramsInt[2] = Integer.parseInt(params[2])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    content += com.ocm.bracelet_machine_sdk.Machine.RobotData.getSetWaterPumpData(paramsInt[0], paramsInt[1], paramsInt[2])
                }
                else -> content += emptyBlock
            }
        }
        while (content.length < 96) content += emptyBlock
        return content
    }
}