package com.ocm.bracelet_machine_sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import com.ocm.bracelet_machine_sdk.Machine.RobotData
import com.ocm.bracelet_machine_sdk.Machine.RobotInterface
import com.ocm.bracelet_machine_sdk.Machine.SerialPortHelper
import com.ocm.bracelet_machine_sdk.processor2.*
import com.ocm.bracelet_machine_sdk.processor2.FetchProcessor
import com.ocm.bracelet_machine_sdk.processor2.GiveBackProcessor
import com.ocm.bracelet_machine_sdk.processor2.InitProcessor
import com.ocm.bracelet_machine_sdk.processor2.SingleGiveBackProcessor
import com.ocm.bracelet_machine_sdk.processor2.TestProcessor
import com.ocm.bracelet_machine_sdk.model.NumModel
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import com.ocm.smartrobot.utils.GPIOHelper
import floatwindow.xishuang.float_lib.FloatLoger
import java.lang.ref.WeakReference
import java.net.NetworkInterface
import java.util.*


/**
 * 手环设备操作
 * 使用前先初始化和自检 init(context) & checkSelf
 */
object BraceletManager2: RobotInterface {


    fun isIC(): Boolean {
        return cardType == BraceletMachineManager.CardType.IC
    }
    internal var cardType = BraceletMachineManager.CardType.IC
    var enableQRFetch = false
        private set
    var enableNFCFetch = false
        private set
    var enableCalc = true
        private set
    var enableAutoRun = false
        private set
    var fetchNum = 1
        private set
    private val cardTypeIsIDKey = "cardTypeKey"
    private val enableQRFetchKey = "enableQRFetchKey"
    private val enableNFCFetchKey = "enableNFCFetchKey"
    private val enableCalcKey = "enableCalcKey"
    private val enableAutoRunKey = "enableAutoRunKey"
    private val fetchNumKey = "fetchNumKey"

    fun setCardType(type: BraceletMachineManager.CardType) {
        cardType = type
        NumberManager2.sharedPreferences?.edit()?.apply {
            putBoolean(cardTypeIsIDKey, !isIC())
        }?.apply()
    }

    fun setEnableQRFetch(enable: Boolean) {
        enableQRFetch = enable
        NumberManager2.sharedPreferences?.edit()?.apply {
            putBoolean(enableQRFetchKey, enable)
        }?.apply()
    }

    fun setEnableNFCFetch(enable: Boolean) {
        enableNFCFetch = enable
        NumberManager2.sharedPreferences?.edit()?.apply {
            putBoolean(enableNFCFetchKey, enable)
        }?.apply()
    }

    fun setEnableCalc(enable: Boolean) {
        enableCalc = enable
        NumberManager2.sharedPreferences?.edit()?.apply {
            putBoolean(enableCalcKey, enable)
        }?.apply()
    }

    fun setEnableAutoRun(enable: Boolean) {
        enableAutoRun = enable
        NumberManager2.sharedPreferences?.edit()?.apply {
            putBoolean(enableAutoRunKey, enable)
        }?.apply()
    }

    fun setFetchNum(num: Int) {
        fetchNum = num
        NumberManager2.sharedPreferences?.edit()?.apply {
            putInt(fetchNumKey, num)
        }?.apply()
    }

    internal var listener: BraceletListener? = null
    private var sysListener: BraceletMachineSystemListener? = null
    var contextReference: WeakReference<Context>? = null

    internal var serialPortHelper: SerialPortHelper? = null

    private val testProcessor = TestProcessor()
    private val initProcessorList = arrayOf(InitProcessor(0), InitProcessor(1))
    private var fetchProcessorList = arrayOf<FetchProcessor>()
    private val givebackProcessor = GiveBackProcessor()
    private val singleGiveBackProcessor = SingleGiveBackProcessor()
    internal var borrowOpt = com.ocm.bracelet_machine_sdk.Machine.OptModel()
    internal var backOpt = com.ocm.bracelet_machine_sdk.Machine.OptModel()
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

    fun loadData(context: Context) {
        NumberManager2.loadForSharedPreferences(context)
        NumberManager2.sharedPreferences?.apply {
            val isID = getBoolean(cardTypeIsIDKey, false)
            cardType = if(isID) BraceletMachineManager.CardType.ID else BraceletMachineManager.CardType.IC
            enableQRFetch = getBoolean(enableQRFetchKey, false)
            enableNFCFetch = getBoolean(enableNFCFetchKey, false)
            enableCalc = getBoolean(enableCalcKey, true)
            enableAutoRun = getBoolean(enableAutoRunKey, false)
            fetchNum = getInt(fetchNumKey, 1)
        }
    }

    /**
     * 绑定context
     * @param context Context
     */
    fun bind(context: Context) {
        LocalLogger.isDebug = true
        loadData(context)
        fetchProcessorList = arrayOf(FetchProcessor(context, 0), FetchProcessor(context, 1))
        contextReference = WeakReference(context)
        serialPortHelper?.close()
        serialPortHelper = SerialPortHelper(
            context,
            object : com.ocm.bracelet_machine_sdk.Machine.MachineInterface {
                override fun onConnect() {
                }

                override fun disConnect() {
                    listener?.onDisconnect()
                }
            })
        //手环数量回调
        NumberManager2.numList.forEach { it.listener = object : NumModel.NumberListener {
            override fun onNoBracelet(addr: Int) {
                fetchProcessorList.getOrNull(addr)?.isStop = true
                serialPortHelper?.SendCmd(RobotData.HOST.STOP, "", addr)
            }

            override fun onNeedRestart(addr: Int) {
            }

            override fun onCurrentNumChange(addr: Int, num: Int) {
                listener?.onCurrentNumChange(addr, num)
            }

        } }
        setupRobotListener()
        serialPortHelper?.Connect()
        try {
            FloatLoger.getInstance().stopServer(context)

        }catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            FloatLoger.ShowFloat = false
            FloatLoger.setSize(500*1024)
            FloatLoger.getInstance().startServer(context)
        }catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            FloatLoger.getInstance().clearOutLog(5)
            FloatLoger.getInstance().hide()
        }catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HardwareIds")
    private fun getLocalMac(context: Context): String {
        var address: String? = null
        // 把当前机器上的访问网络接口的存入 Enumeration集合中
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val netWork = interfaces.nextElement()
            // 如果存在硬件地址并可以使用给定的当前权限访问，则返回该硬件地址（通常是 MAC）。
            val by = netWork.hardwareAddress
            if (by == null || by.isEmpty()) {
                continue
            }
            val builder = StringBuilder()
            for (b in by) {
                builder.append(String.format("%02x:", b))
            }
            if (builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
            val mac = builder.toString()
            // 从路由器上在线设备的MAC地址列表，可以印证设备Wifi的 name 是 wlan0
            if (netWork.name == "wlan0") {
                address = mac
            }
        }
        return address ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
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
     * 配置归还超时时间
     * @param timeout Long 秒
     */
    fun setGiveBackTimeout(timeout: Int) {
        givebackProcessor.timeout = timeout
    }

    /**
     * 释放资源
     */
    fun onDestroy() {
        serialPortHelper?.close()
    }

    /**
     * 打开扫码器
     */
    fun openQR() {
        GPIOHelper.relayOn()
    }

    /**
     * 关闭扫码器
     */
    fun closeQR() {
        GPIOHelper.relayOff()
    }

    /**
     * 打开 LED 补光灯
     */
    fun openLedLight() {
        GPIOHelper.ledOn()
    }

    /**
     * 关闭 LED 补光灯
     */
    fun closeLedLight() {
        GPIOHelper.ledOff()
    }

    /**
     * 打开红外补光灯，用于红外活体检测
     */
    fun openIRLight() {
        GPIOHelper.cameraRedOn()
    }

    /**
     * 关闭红外补光灯
     */
    fun closeIRLight() {
        GPIOHelper.cameraRedOff()
    }

    /**
     * 获取手环机状态
     */
    fun fetchStatus(callback: CheckStatusCallback) {
        if (machineState != BraceletMachineManager.MachineState.IDLE) {
            callback.onSuccess(BraceletMachineManager.BraceletMachineStatus.BUSY)
            callback.onCompleted()
            return
        }
        machineState = BraceletMachineManager.MachineState.TEST
        testProcessor.setCallback(callback)
        testProcessor.start()
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
    fun checkSelf(addrIndex: Int, type: BraceletMachineManager.CardType, callback: CheckSelfCallback) {
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
    internal fun processDone() {
        LocalLogger.write("状态重置")
        fetchProcessorList.getOrNull(addrIndex)?.destory()
        machineState = BraceletMachineManager.MachineState.IDLE
    }

    fun destoryGiveBack() {
        singleGiveBackProcessor.destory()
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
        fetchBracelet(addrIndex, "01", 1,  BraceletMachineManager.SectorType.SECTOR2, borrowOpt.pwd, borrowOpt.Content(), callback)
    }

    /***
     * 获取手环，并写入数据
     *
     * @param sector SectorType 扇区，输入SECTOR1-15
     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
     * @param content String 写入0-2块的内容，48个字节内
     * @param callback FetchCallback 获取回调
     */
    fun fetchBracelet(addrIndex: Int, sector: BraceletMachineManager.SectorType, pwd: String, content: String, callback: FetchCallback) {
        fetchBracelet(addrIndex, "03", 1, sector, pwd, content, callback)
    }

    /***
     * 获取多个手环
     */
    fun fetchMultiBracelet(addrIndex: Int, num: Int, callback: FetchCallback) {
        fetchBracelet(addrIndex, "01", num,  BraceletMachineManager.SectorType.SECTOR2, borrowOpt.pwd, borrowOpt.Content(), callback)
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
    fun fetchMultiBracelet(addrIndex: Int, num: Int, sector: BraceletMachineManager.SectorType, pwd: String, content: String, callback: FetchCallback) {
        fetchBracelet(addrIndex, "03", num, sector, pwd, content, callback)
    }

    private fun fetchBracelet(addrIndex: Int, type: String, num: Int, sector: BraceletMachineManager.SectorType, pwd: String, content: String, callback: FetchCallback) {
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
                    callback.onRemainingFetch(num-1)
                    fetchBracelet(addrIndex, type, num-1, sector, pwd, content, callback)
                } else {
                    callback.onCompleted()
                }

            }, 5000)
            return
        }
        fetchProcessorList.getOrNull(addrIndex)?.fetchCount = num
        fetchProcessorList.getOrNull(addrIndex)?.setCallback(callback)
        machineState = BraceletMachineManager.MachineState.FETCHING
        fetchProcessorList.getOrNull(addrIndex)?.fetch("$type${sector.cmd}$pwd$content$supplementZero")
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
        if ((NumberManager2.numList.getOrNull(addrIndex)?.maxNum ?: 0) <= (NumberManager2.numList.getOrNull(addrIndex)?.currentNum ?: 0)) {
            return true
        }
        return false
    }

//    /**
//     * 还手环
//     */
//    fun giveBackBracelet(callback: GiveBackCallback) {
//        giveBackBracelet("01", SectorType.SECTOR2, "FFFFFFFFFFFF", "", callback)
//    }
//
//    /***
//     * 还手环，并写入数据
//     *
//     * @param sector SectorType 扇区，输入SECTOR1-15
//     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
//     * @param content String 写入0-2块的内容，48个字节内
//     * @param callback GiveBackCallback 获取回调
//     */
//    fun giveBackBracelet(sector: SectorType, pwd: String, content: String, callback: GiveBackCallback) {
//        giveBackBracelet("03", sector, pwd, content, callback)
//    }
//
//    private fun giveBackBracelet(type: String, sector: SectorType, pwd: String, content: String, callback: GiveBackCallback) {
//        val checkStatus = checkStatus()
//        if (checkStatus != null) {
//            callback.onGiveBackFail(checkStatus)
//            callback.onCompleted()
//            return
//        }
//        if (checkIsFull(0)) {
//            callback.onGiveBackFail("手环已满")
//            callback.onCompleted()
//            return
//        }
//        if (pwd.count() != 12) {
//            callback.onGiveBackFail("密码位数错误")
//            callback.onCompleted()
//            return
//        }
//        if (content.count() > 96) {
//            callback.onGiveBackFail("写入内容太长")
//            callback.onCompleted()
//            return
//        }
//        machineState = MachineState.IN_GIVE_BACK
//        var supplementZero = ""//不足补零
//        if (content.count() < 96) {
//            val supplement = 96 - content.count()
//            for (index in 0 until supplement)
//                supplementZero = "${supplementZero}0"
//        }
//        givebackProcessor.backOpt.key = sector.cmd
//        givebackProcessor.backOpt.pwd = pwd
//        givebackProcessor.backOpt.type = type
//        givebackProcessor.backOpt.sectorContent = content+supplementZero
//        givebackProcessor.setCallback(callback)
//        givebackProcessor.start()
//    }
//
//    /**
//     * 主动停止还手环
//     */
//    fun stopGiveBack() {
//        givebackProcessor.stop()
//        processDone()
//    }
//
//    /**
//     * 读取回收处卡号
//     */
//    fun readRecyclingNo(callback: WRGiveBackCallback) {
//        val checkStatus = checkStatus()
//        if (checkStatus != null) {
//            callback.onGiveBackFail(checkStatus)
//            callback.onCompleted()
//            return
//        }
//        if (checkIsFull(0)) {
//            callback.onGiveBackFail("手环已满")
//            callback.onCompleted()
//            return
//        }
//        machineState = MachineState.IN_SINGLE_GIVE_BACK
//        singleGiveBackProcessor.readNo(callback)
//    }
//
//    /**
//     * 读取回收处卡号和扇区
//     */
//    fun readRecyclingInfo(sector: SectorType, pwd: String, callback: WRGiveBackCallback) {
//        val checkStatus = checkStatus()
//        if (checkStatus != null) {
//            callback.onGiveBackFail(checkStatus)
//            callback.onCompleted()
//            return
//        }
//        if (checkIsFull(0)) {
//            callback.onGiveBackFail("手环已满")
//            callback.onCompleted()
//            return
//        }
//        if (pwd.count() != 12) {
//            callback.onGiveBackFail("密码位数错误")
//            callback.onCompleted()
//            return
//        }
//        machineState = MachineState.IN_SINGLE_GIVE_BACK
//        singleGiveBackProcessor.readInfo(sector, pwd, callback)
//    }
//
//    /**
//     * 读取回收处卡号并写扇区
//     */
//    fun readRecyclingNoAndWriteSector(sector: SectorType, pwd: String, content: String, callback: WRGiveBackCallback) {
//        val checkStatus = checkStatus()
//        if (checkStatus != null) {
//            callback.onGiveBackFail(checkStatus)
//            callback.onCompleted()
//            return
//        }
//        if (checkIsFull(0)) {
//            callback.onGiveBackFail("手环已满")
//            callback.onCompleted()
//            return
//        }
//        if (pwd.count() != 12) {
//            callback.onGiveBackFail("密码位数错误")
//            callback.onCompleted()
//            return
//        }
//        if (content.count() > 96) {
//            callback.onGiveBackFail("写入内容太长")
//            callback.onCompleted()
//            return
//        }
//        var supplementZero = ""//不足补零
//        if (content.count() < 96) {
//            val supplement = 96 - content.count()
//            for (index in 0 until supplement)
//                supplementZero = "${supplementZero}0"
//        }
//        machineState = MachineState.IN_SINGLE_GIVE_BACK
//        singleGiveBackProcessor.readNoAndWriteSector(sector, pwd, content + supplementZero, callback)
//    }
//
//    fun openRecycling(callback: AllowGiveBackCallback) {
//        val checkStatus = checkStatus()
//        if (checkStatus != null) {
//            callback.onFail(checkStatus)
//            callback.onCompleted()
//            return
//        }
//        if (checkIsFull(0)) {
//            callback.onFail("手环已满")
//            callback.onCompleted()
//            return
//        }
//        machineState = MachineState.IN_SINGLE_GIVE_BACK
//        singleGiveBackProcessor.giveback(callback)
//    }
//
//    /**
//     * 系统放置手环
//     */
//    fun sysStartPush(listener: BraceletMachineSystemListener) {
//        if (isSyspush || isDebug) {
//            listener.onSuccess()
//            return
//        }
//        isSyspush = true
//        LocalLogger.write("放置手环")
//        sysListener = listener
//        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.PUSHBRAND,"", 0)
//    }
//
//    /**
//     * 系统停止放置手环
//     */
//    fun sysStopPush(listener: BraceletMachineSystemListener) {
//        if (!isSyspush || isDebug) {
//            listener.onSuccess()
//            return
//        }
//        isSyspush = false
//        LocalLogger.write("停止放置手环")
//        sysListener = listener
//        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.PUSHBRANDOVER,"", 0)
//    }

    private fun checkStatus(addrIndex: Int): String? {
        if (contextReference == null) {
            return "请先使用 bind 方法，绑定 context"
        }
        if (initProcessorList.getOrNull(addrIndex)?.initDone == false) {
            return "设备未自检"
        }
        if (machineState != BraceletMachineManager.MachineState.IDLE) {
            return when(machineState) {
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

    //配置信息回调监听
    private fun setupRobotListener() {
        serialPortHelper?.setOnMsg(this)
        serialPortHelper?.setOnSysMsg { msg, _ ->
            if (msg == null) return@setOnSysMsg
            when(msg) {
                com.ocm.bracelet_machine_sdk.Machine.RobotSysMsg.Success -> sysListener?.onSuccess()
                com.ocm.bracelet_machine_sdk.Machine.RobotSysMsg.Fail -> sysListener?.onFail()
            }
        }
    }

    override fun OnMsg(msg: com.ocm.bracelet_machine_sdk.Machine.RobotMsg?, data: Any?) {
        LocalLogger.write("OnMsg - $msg")
        initProcessorList.getOrNull(addrIndex)?.OnMsg(msg, data)
        fetchProcessorList.getOrNull(addrIndex)?.OnMsg(msg, data)
        if (machineState == BraceletMachineManager.MachineState.IN_GIVE_BACK) {
            givebackProcessor.OnMsg(msg, data)
        } else {
            singleGiveBackProcessor.OnMsg(msg, data)
        }
        testProcessor.OnMsg(msg, data)
        robotListener?.OnMsg(msg, data)
    }
}