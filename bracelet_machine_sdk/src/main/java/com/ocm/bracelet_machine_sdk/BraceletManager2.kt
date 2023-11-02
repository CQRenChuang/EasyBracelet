package com.ocm.bracelet_machine_sdk

import android.content.Context
import android.os.Handler
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.cardType
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.serialPortHelper
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.setCardType
import com.ocm.bracelet_machine_sdk.Machine.RobotData
import com.ocm.bracelet_machine_sdk.Machine.RobotInterface
import com.ocm.bracelet_machine_sdk.processor2.*
import com.ocm.bracelet_machine_sdk.model.NumModel
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import java.lang.ref.WeakReference
import java.util.*


/**
 * 手环设备操作
 * 使用前先初始化和自检 init(context) & checkSelf
 */
object BraceletManager2 {

    internal var listener: BraceletListener? = null
    private var sysListener: BraceletMachineSystemListener? = null
    var contextReference: WeakReference<Context>? = null

    private val testProcessor = TestProcessor()
    private val initProcessorList = arrayOf(InitProcessor(0), InitProcessor(1))
    private var fetchProcessorList = arrayOf<FetchProcessor>()
    internal var borrowOpt = com.ocm.bracelet_machine_sdk.Machine.OptModel()
    internal var machineState = BraceletMachineManager.MachineState.IDLE
    internal var isSyspush = false
    private val handler = Handler()
    private var uploadLogTimer: Timer? = null
    var robotListener: RobotInterface? = null
    private var addrIndex = 0

    val versionDefine = "1.0.1"

    /**
     * 调试模式
     */
    var isDebug = false

    /**
     * 最大手环数量
     */
    fun getMaxBracelet(addrIndex: Int): Int {
        return NumberManager2.numList.getOrNull(addrIndex)?.maxNum ?: 0
    }

    /**
     * 当前剩余手环数量
     */
    fun getCurrentBracelet(addrIndex: Int): Int {
        return NumberManager2.numList.getOrNull(addrIndex)?.currentNum ?: 0
    }

    fun setCurrentBracelet(addrIndex: Int, num: Int) {
        NumberManager2.numList.getOrNull(addrIndex)?.currentNum = num
    }

    /**
     * 绑定context
     * @param context Context
     */
    internal fun bind(context: Context) {
        LocalLogger.isDebug = true
        fetchProcessorList = arrayOf(FetchProcessor(context, 0), FetchProcessor(context, 1))
        contextReference = WeakReference(context)
        //手环数量回调
        NumberManager2.numList.forEach {
            it.listener = object : NumModel.NumberListener {
                override fun onNoBracelet(addr: Int) {
                    fetchProcessorList.getOrNull(addr)?.isStop = true
                    serialPortHelper?.SendCmd(RobotData.HOST.STOP, "", addr)
                }

                override fun onNeedRestart(addr: Int) {
                }

                override fun onCurrentNumChange(addr: Int, num: Int) {
                    listener?.onCurrentNumChange(addr, num)
                }

            }
            it.loadForSharedPreferences(context)
        }
    }

    /**
     * 开始转动手环机
     */
    fun start(addrIndex: Int) {
        fetchProcessorList.getOrNull(addrIndex)?.isStop = true
        LocalLogger.write("开始转动滚筒")
        serialPortHelper?.SendCmd(RobotData.HOST.START, "", 0)
        listener?.onStateChange(addrIndex, true)
    }

    /**
     * 停止转动滚筒
     */
    fun stopRoll(addrIndex: Int) {
        fetchProcessorList.getOrNull(addrIndex)?.isStop = true
        serialPortHelper?.SendCmd(RobotData.HOST.STOP, "", addrIndex)
        LocalLogger.write("主动停止转动滚筒")
        listener?.onStateChange(addrIndex, true)
        processDone()
    }

    /**
     * 自检
     */
    fun checkSelf(
        addrIndex: Int,
        type: BraceletMachineManager.CardType,
        callback: CheckSelfCallback
    ) {
        setCardType(type)
        checkSelf(addrIndex, callback)
    }


    fun checkSelf(addrIndex: Int, callback: CheckSelfCallback) {
        this.addrIndex = addrIndex
        if (machineState == BraceletMachineManager.MachineState.IN_CHECK_SELF) {
            callback.onCheckSelfFail("正在自检中")
            callback.onCompleted()
            return
        }
        if (isDebug) {
            handler.postDelayed({
                initProcessorList.getOrNull(addrIndex)?.initDone = true
                callback.onCheckSelfSuccess()
                callback.onCompleted()

            }, 1000)
            return
        }
        machineState = BraceletMachineManager.MachineState.IN_CHECK_SELF
        initProcessorList.getOrNull(addrIndex)?.setCallback(callback)
        handler.postDelayed({
            initProcessorList.getOrNull(addrIndex)?.start(cardType)

        }, 1000)
    }

    //操作处理结束
    fun processDone() {
        LocalLogger.write("状态重置")
        fetchProcessorList.getOrNull(addrIndex)?.destory()
        machineState = BraceletMachineManager.MachineState.IDLE
    }

    /**
     * 设置回调监听
     * @param listener BraceletMachineListener
     */
    fun setBraceletMachineListener(listener: BraceletListener) {
        BraceletManager2.listener = listener
    }

    /**
     * 检查是否还有手环
     * @return Boolean
     */
    fun checkHasBracelet(addrIndex: Int): Boolean {
        if ((NumberManager2.numList.getOrNull(addrIndex)?.currentNum ?: 0) <= 0) {
            return false
        }
        return true
    }

    private fun checkHasBracelet(addrIndex: Int, num: Int): Boolean {
        if ((NumberManager2.numList.getOrNull(addrIndex)?.currentNum ?: 0) < num) {
            return false
        }
        return true
    }

    /**
     * 获取手环
     */
    fun fetchBracelet(addrIndex: Int, callback: FetchCallback) {
        fetchBracelet(
            addrIndex,
            "01",
            1,
            BraceletMachineManager.SectorType.SECTOR2,
            borrowOpt.pwd,
            borrowOpt.Content(),
            callback
        )
    }

    /***
     * 获取手环，并写入数据
     *
     * @param sector SectorType 扇区，输入SECTOR1-15
     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
     * @param content String 写入0-2块的内容，48个字节内
     * @param callback FetchCallback 获取回调
     */
    fun fetchBracelet(
        addrIndex: Int,
        sector: BraceletMachineManager.SectorType,
        pwd: String,
        content: String,
        callback: FetchCallback
    ) {
        fetchBracelet(addrIndex, "03", 1, sector, pwd, content, callback)
    }

    /***
     * 获取多个手环
     */
    fun fetchMultiBracelet(addrIndex: Int, num: Int, callback: FetchCallback) {
        fetchBracelet(
            addrIndex,
            "01",
            num,
            BraceletMachineManager.SectorType.SECTOR2,
            borrowOpt.pwd,
            borrowOpt.Content(),
            callback
        )
    }

    /***
     * 获取多个手环，并写入数据
     *
     * @param sector SectorType 扇区，输入SECTOR1-15
     * @param num Int 获取手环数量
     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
     * @param content String 写入0-2块的内容，48个字节内
     * @param callback FetchCallback 获取回调
     */
    fun fetchMultiBracelet(
        addrIndex: Int,
        num: Int,
        sector: BraceletMachineManager.SectorType,
        pwd: String,
        content: String,
        callback: FetchCallback
    ) {
        fetchBracelet(addrIndex, "03", num, sector, pwd, content, callback)
    }

    private fun fetchBracelet(
        addrIndex: Int,
        type: String,
        num: Int,
        sector: BraceletMachineManager.SectorType,
        pwd: String,
        content: String,
        callback: FetchCallback
    ) {
        this.addrIndex = addrIndex
        val checkStatus = checkStatus(addrIndex)
        if (checkStatus != null) {
            callback.onFetchFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (!checkHasBracelet(addrIndex, num)) {
            callback.onFetchFail("剩余手环不足")
            callback.onCompleted()
            return
        }
        if (pwd.count() != 12) {
            callback.onFetchFail("密码位数错误")
            callback.onCompleted()
            return
        }
        if (content.count() > 96) {
            callback.onFetchFail("写入内容太长")
            callback.onCompleted()
            return
        }
        callback.onBeginFetch()
        var supplementZero = ""//不足补零
        if (content.count() < 96) {
            val supplement = 96 - content.count()
            for (index in 0 until supplement)
                supplementZero = "${supplementZero}0"
        }
        if (isDebug) {
            handler.postDelayed({
                processDone()
                callback.onFetchSuccess("1234_$num", "hex1234")
                NumberManager2.desCurrentNum(addrIndex)
                if (num > 1) {
                    callback.onRemainingFetch(num - 1)
                    fetchBracelet(addrIndex, type, num - 1, sector, pwd, content, callback)
                } else {
                    callback.onCompleted()
                }

            }, 5000)
            return
        }
        fetchProcessorList.getOrNull(addrIndex)?.fetchCount = num
        fetchProcessorList.getOrNull(addrIndex)?.setCallback(callback)
        machineState = BraceletMachineManager.MachineState.FETCHING
        fetchProcessorList.getOrNull(addrIndex)
            ?.fetch("$type${sector.cmd}$pwd$content$supplementZero")
    }

    /**
     * 主动停止取手环
     */
    fun stopFetch() {
        fetchProcessorList.getOrNull(addrIndex)?.stop()
    }

    /**
     * 检查机器是否装满了手环
     * @return Boolean
     */
    fun checkIsFull(addrIndex: Int): Boolean {
        if ((NumberManager2.numList.getOrNull(addrIndex)?.maxNum
                ?: 0) <= (NumberManager2.numList.getOrNull(addrIndex)?.currentNum ?: 0)
        ) {
            return true
        }
        return false
    }

    private fun checkStatus(addrIndex: Int): String? {
        if (contextReference == null) {
            return "请先使用 bind 方法，绑定 context"
        }
        if (initProcessorList.getOrNull(addrIndex)?.initDone == false) {
            return "设备未自检"
        }
        if (machineState != BraceletMachineManager.MachineState.IDLE) {
            return when (machineState) {
                BraceletMachineManager.MachineState.FETCHING -> "正在获取手环"
                BraceletMachineManager.MachineState.IN_CHECK_SELF -> "正在自检中"
                BraceletMachineManager.MachineState.IN_GIVE_BACK -> "正在归还手环"
                BraceletMachineManager.MachineState.IN_SINGLE_GIVE_BACK -> "正在单步归还手环"
                BraceletMachineManager.MachineState.TEST -> "正在测试连通性"
                else -> null
            }
        }
        return null
    }

    fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        initProcessorList.getOrNull(addrIndex)?.OnMsg(msg, data)
        fetchProcessorList.getOrNull(addrIndex)?.OnMsg(msg, data)
        testProcessor.OnMsg(msg, data)
        robotListener?.OnMsg(msg, data)
    }
}