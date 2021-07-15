package com.ocm.bracelet_machine_sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.provider.Settings
import com.ocm.bracelet_machine_sdk.Machine.RobotData
import com.ocm.bracelet_machine_sdk.Machine.RobotInterface
import com.ocm.bracelet_machine_sdk.processor.*
import com.ocm.bracelet_machine_sdk.processor.FetchProcessor
import com.ocm.bracelet_machine_sdk.processor.GiveBackProcessor
import com.ocm.bracelet_machine_sdk.processor.InitProcessor
import com.ocm.bracelet_machine_sdk.processor.SingleGiveBackProcessor
import com.ocm.bracelet_machine_sdk.processor.TestProcessor
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
object BraceletMachineManager: RobotInterface {

    internal enum class MachineState {
        IDLE, IN_CHECK_SELF, FETCHING, IN_GIVE_BACK, IN_SINGLE_GIVE_BACK, TEST
    }

    /**
     * 卡片类型
     */
    enum class CardType {
        IC , ID , UNIVERSAL;
        var cmd: String = "01"
            get() {
                return when(this) {
                    IC -> "01"
                    ID -> "02"
                    UNIVERSAL -> "03"
                }
            }
            private set
    }

    /**
     * 手环收发模式
     */
    enum class BraceletMode {
        ONLINE , OFFLINE
    }

    /**
     * 手环机状态
     */
    enum class BraceletMachineStatus {
        HAS_CACHE , //有缓存
        NO_CACHE, //没有缓存
        BUSY //忙碌
    }

    fun isIC(): Boolean {
        return cardType == CardType.IC
    }
    internal var cardType = CardType.IC
    var enableQRFetch = false
        private set
    var enableNFCFetch = false
        private set
    private val cardTypeIsIDKey = "cardTypeKey"
    private val enableQRFetchKey = "enableQRFetchKey"
    private val enableNFCFetchKey = "enableNFCFetchKey"

    fun setCardType(type: CardType) {
        cardType = type
        BraceletNumberManager.sharedPreferences?.edit()?.apply {
            putBoolean(cardTypeIsIDKey, !isIC())
        }?.apply()
    }

    fun setEnableQRFetch(enable: Boolean) {
        enableQRFetch = enable
        BraceletNumberManager.sharedPreferences?.edit()?.apply {
            putBoolean(enableQRFetchKey, enable)
        }?.apply()
    }

    fun setEnableNFCFetch(enable: Boolean) {
        enableNFCFetch = enable
        BraceletNumberManager.sharedPreferences?.edit()?.apply {
            putBoolean(enableNFCFetchKey, enable)
        }?.apply()
    }

    /**
     * 允许写的扇区
     */
    enum class SectorType {
        SECTOR1, SECTOR2, SECTOR3, SECTOR4, SECTOR5,
        SECTOR6, SECTOR7, SECTOR8, SECTOR9, SECTOR10,
        SECTOR11, SECTOR12, SECTOR13, SECTOR14, SECTOR15;
        var cmd: String = "01"
            get() {
                return when(this) {
                    SECTOR1 -> "01"
                    SECTOR2 -> "02"
                    SECTOR3 -> "03"
                    SECTOR4 -> "04"
                    SECTOR5 -> "05"
                    SECTOR6 -> "06"
                    SECTOR7 -> "07"
                    SECTOR8 -> "08"
                    SECTOR9 -> "09"
                    SECTOR10 -> "0A"
                    SECTOR11 -> "0B"
                    SECTOR12 -> "0C"
                    SECTOR13 -> "0D"
                    SECTOR14 -> "0E"
                    SECTOR15 -> "0F"
                }
            }
            private set
    }

    internal var listener: BraceletMachineListener? = null
    private var sysListener: BraceletMachineSystemListener? = null
    var contextReference: WeakReference<Context>? = null

    internal var serialPortHelper: com.ocm.bracelet_machine_sdk.Machine.SerialPortHelper? = null

    private val testProcessor = TestProcessor()
    private val initProcessor = InitProcessor()
    private lateinit var fetchProcessor: FetchProcessor
    private val givebackProcessor = GiveBackProcessor()
    private val singleGiveBackProcessor = SingleGiveBackProcessor()
    internal var borrowOpt = com.ocm.bracelet_machine_sdk.Machine.OptModel()
    internal var backOpt = com.ocm.bracelet_machine_sdk.Machine.OptModel()
    internal var machineState = MachineState.IDLE
    internal var isSyspush = false
    private val handler = Handler()
    private var uploadLogTimer: Timer? = null
    var robotListener: RobotInterface? = null

    /**
     * 调试模式
     */
    var isDebug = false

    /**
     * 最大手环数量
     */
    var maxBracelet: Int
        get() = BraceletNumberManager.maxNum
        set(value) {
            BraceletNumberManager.maxNum = value
        }

    /**
     * 当前剩余手环数量
     */
    var currentBracelet: Int
        get() = BraceletNumberManager.currentNum
        set(value) {
            BraceletNumberManager.currentNum = value
        }

    /**
     * 绑定context
     * @param context Context
     */
    fun bind(context: Context) {
        LocalLogger.isDebug = true
        BraceletNumberManager.loadForSharedPreferences(context)
        BraceletNumberManager.sharedPreferences?.apply {
            val isID = getBoolean(cardTypeIsIDKey, false)
            cardType = if(isID) CardType.ID else CardType.IC
            enableQRFetch = getBoolean(enableQRFetchKey, false)
            enableNFCFetch = getBoolean(enableNFCFetchKey, false)

        }
        fetchProcessor = FetchProcessor(context)
        contextReference = WeakReference(context)
        serialPortHelper?.close()
        serialPortHelper = com.ocm.bracelet_machine_sdk.Machine.SerialPortHelper(
            context,
            object : com.ocm.bracelet_machine_sdk.Machine.MachineInterface {
                override fun onConnect() {
                }

                override fun disConnect() {
                    listener?.onDisconnect()
                }
            })
        //手环数量回调
        BraceletNumberManager.listener = object : BraceletNumberManager.BraceletNumberManagerListener {
            override fun onCurrentNumChange(num: Int) {
                    listener?.onCurrentNumChange(num)
            }

            override fun onNoBracelet() {
                fetchProcessor.isStop = true
                serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.STOP, "")
            }

            override fun onNeedRestart() {
                if (!fetchProcessor.isStop) return
                fetchProcessor.isStop = false
                listener?.onStateChange(false)
                serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.START, "")
            }
        }
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
            FloatLoger.getInstance().clearOutLog(5)
            FloatLoger.getInstance().startServer(context)
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
    fun start() {
        fetchProcessor.isStop = true
        LocalLogger.write("开始转动滚筒")
        serialPortHelper?.SendCmd(RobotData.HOST.START, "")
        listener?.onStateChange(true)
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
        if (machineState != MachineState.IDLE) {
            callback.onSuccess(BraceletMachineStatus.BUSY)
            callback.onCompleted()
            return
        }
        machineState = MachineState.TEST
        testProcessor.setCallback(callback)
        testProcessor.start()
    }

    /**
     * 停止转动滚筒
     */
    fun stopRoll() {
        fetchProcessor.isStop = true
        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.STOP, "")
        LocalLogger.write("主动停止转动滚筒")
        listener?.onStateChange(true)
        processDone()
    }

    /**
     * 自检
     */
    fun checkSelf(callback: CheckSelfCallback) {
        if (machineState == MachineState.IN_CHECK_SELF) {
            callback.onCheckSelfFail("正在自检中")
            callback.onCompleted()
            return
        }
        if (isDebug) {
            handler.postDelayed({
                initProcessor.initDone = true
                callback.onCheckSelfSuccess()
                callback.onCompleted()

            }, 1000)
            return
        }
        machineState = MachineState.IN_CHECK_SELF
        initProcessor.setCallback(callback)
        handler.postDelayed({
            initProcessor.start(cardType)

        }, 1000)
    }

    //操作处理结束
    internal fun processDone() {
        machineState = MachineState.IDLE
    }

    /**
     * 设置回调监听
     * @param listener BraceletMachineListener
     */
    fun setBraceletMachineListener(listener: BraceletMachineListener) {
        BraceletMachineManager.listener = listener
    }

    /**
     * 检查是否还有手环
     * @return Boolean
     */
    fun checkHasBracelet(): Boolean {
        if (currentBracelet <= 0) {
            return false
        }
        return true
    }

    private fun checkHasBracelet(num: Int): Boolean {
        if (currentBracelet < num) {
            return false
        }
        return true
    }

    /**
     * 获取手环
     */
    fun fetchBracelet(callback: FetchCallback) {
        fetchBracelet("01", 1,  SectorType.SECTOR2, borrowOpt.pwd, borrowOpt.Content(), callback)
    }

    /***
     * 获取手环，并写入数据
     *
     * @param sector SectorType 扇区，输入SECTOR1-15
     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
     * @param content String 写入0-2块的内容，48个字节内
     * @param callback FetchCallback 获取回调
     */
    fun fetchBracelet(sector: SectorType, pwd: String, content: String, callback: FetchCallback) {
        fetchBracelet("03", 1, sector, pwd, content, callback)
    }

    /***
     * 获取多个手环
     */
    fun fetchMultiBracelet(num: Int, callback: FetchCallback) {
        fetchBracelet("01", num,  SectorType.SECTOR2, borrowOpt.pwd, borrowOpt.Content(), callback)
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
    fun fetchMultiBracelet(num: Int, sector: SectorType, pwd: String, content: String, callback: FetchCallback) {
        fetchBracelet("03", num, sector, pwd, content, callback)
    }

    private fun fetchBracelet(type: String, num: Int, sector: SectorType, pwd: String, content: String, callback: FetchCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onFetchFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (!checkHasBracelet(num)) {
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
        var supplementZero = ""//不足补零
        if (content.count() < 96) {
            val supplement = 96 - content.count()
            for (index in 0 until supplement)
                supplementZero = "${supplementZero}0"
        }
        if (isDebug) {
            handler.postDelayed({
                processDone()
                callback.onFetchSuccess("1234_$num")
                BraceletNumberManager.desCurrentNum()
                if (num > 1) {
                    callback.onRemainingFetch(num-1)
                    fetchBracelet(type, num-1, sector, pwd, content, callback)
                } else {
                    callback.onCompleted()
                }
                callback.onCompleted()

            }, 1000)
            return
        }
        fetchProcessor.fetchCount = num
        fetchProcessor.setCallback(callback)
        machineState = MachineState.FETCHING
        fetchProcessor.fetch("$type${sector.cmd}$pwd$content$supplementZero")
    }

    /**
     * 主动停止取手环
     */
    fun stopFetch() {
        fetchProcessor.stop()
    }

    /**
     * 检查机器是否装满了手环
     * @return Boolean
     */
    fun checkIsFull(): Boolean {
        if (maxBracelet <= currentBracelet) {
            return true
        }
        return false
    }

    /**
     * 还手环
     */
    fun giveBackBracelet(callback: GiveBackCallback) {
        giveBackBracelet("01", SectorType.SECTOR2, "FFFFFFFFFFFF", "", callback)
    }

    /***
     * 还手环，并写入数据
     *
     * @param sector SectorType 扇区，输入SECTOR1-15
     * @param pwd String 密码，6字节16进制字符串，如默认："FFFFFFFFFFFF"
     * @param content String 写入0-2块的内容，48个字节内
     * @param callback GiveBackCallback 获取回调
     */
    fun giveBackBracelet(sector: SectorType, pwd: String, content: String, callback: GiveBackCallback) {
        giveBackBracelet("03", sector, pwd, content, callback)
    }

    private fun giveBackBracelet(type: String, sector: SectorType, pwd: String, content: String, callback: GiveBackCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onGiveBackFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (checkIsFull()) {
            callback.onGiveBackFail("手环已满")
            callback.onCompleted()
            return
        }
        if (pwd.count() != 12) {
            callback.onGiveBackFail("密码位数错误")
            callback.onCompleted()
            return
        }
        if (content.count() > 96) {
            callback.onGiveBackFail("写入内容太长")
            callback.onCompleted()
            return
        }
        machineState = MachineState.IN_GIVE_BACK
        var supplementZero = ""//不足补零
        if (content.count() < 96) {
            val supplement = 96 - content.count()
            for (index in 0 until supplement)
                supplementZero = "${supplementZero}0"
        }
        givebackProcessor.backOpt.key = sector.cmd
        givebackProcessor.backOpt.pwd = pwd
        givebackProcessor.backOpt.type = type
        givebackProcessor.backOpt.sectorContent = content+supplementZero
        givebackProcessor.setCallback(callback)
        givebackProcessor.start()
    }

    /**
     * 主动停止还手环
     */
    fun stopGiveBack() {
        givebackProcessor.stop()
    }

    /**
     * 读取回收处卡号
     */
    fun readRecyclingNo(callback: WRGiveBackCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onGiveBackFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (checkIsFull()) {
            callback.onGiveBackFail("手环已满")
            callback.onCompleted()
            return
        }
        machineState = MachineState.IN_SINGLE_GIVE_BACK
        singleGiveBackProcessor.readNo(callback)
    }

    /**
     * 读取回收处卡号和扇区
     */
    fun readRecyclingInfo(sector: SectorType, pwd: String, callback: WRGiveBackCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onGiveBackFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (checkIsFull()) {
            callback.onGiveBackFail("手环已满")
            callback.onCompleted()
            return
        }
        if (pwd.count() != 12) {
            callback.onGiveBackFail("密码位数错误")
            callback.onCompleted()
            return
        }
        machineState = MachineState.IN_SINGLE_GIVE_BACK
        singleGiveBackProcessor.readInfo(sector, pwd, callback)
    }

    /**
     * 读取回收处卡号并写扇区
     */
    fun readRecyclingNoAndWriteSector(sector: SectorType, pwd: String, content: String, callback: WRGiveBackCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onGiveBackFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (checkIsFull()) {
            callback.onGiveBackFail("手环已满")
            callback.onCompleted()
            return
        }
        if (pwd.count() != 12) {
            callback.onGiveBackFail("密码位数错误")
            callback.onCompleted()
            return
        }
        if (content.count() > 96) {
            callback.onGiveBackFail("写入内容太长")
            callback.onCompleted()
            return
        }
        var supplementZero = ""//不足补零
        if (content.count() < 96) {
            val supplement = 96 - content.count()
            for (index in 0 until supplement)
                supplementZero = "${supplementZero}0"
        }
        machineState = MachineState.IN_SINGLE_GIVE_BACK
        singleGiveBackProcessor.readNoAndWriteSector(sector, pwd, content + supplementZero, callback)
    }

    fun openRecycling(callback: AllowGiveBackCallback) {
        val checkStatus = checkStatus()
        if (checkStatus != null) {
            callback.onFail(checkStatus)
            callback.onCompleted()
            return
        }
        if (checkIsFull()) {
            callback.onFail("手环已满")
            callback.onCompleted()
            return
        }
        machineState = MachineState.IN_SINGLE_GIVE_BACK
        singleGiveBackProcessor.giveback(callback)
    }

    /**
     * 系统放置手环
     */
    fun sysStartPush(listener: BraceletMachineSystemListener) {
        if (isSyspush || isDebug) {
            listener.onSuccess()
            return
        }
        isSyspush = true
        LocalLogger.write("放置手环")
        sysListener = listener
        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.PUSHBRAND,"")
    }

    /**
     * 系统停止放置手环
     */
    fun sysStopPush(listener: BraceletMachineSystemListener) {
        if (!isSyspush || isDebug) {
            listener.onSuccess()
            return
        }
        isSyspush = false
        LocalLogger.write("停止放置手环")
        sysListener = listener
        serialPortHelper?.SendCmd(com.ocm.bracelet_machine_sdk.Machine.RobotData.HOST.PUSHBRANDOVER,"")
    }

    private fun checkStatus(): String? {
        if (contextReference == null) {
            return "请先使用 bind 方法，绑定 context"
        }
        if (!initProcessor.initDone) {
            return "设备未自检"
        }
        if (machineState != MachineState.IDLE) {
            return when(machineState) {
                MachineState.FETCHING -> "正在获取手环"
                MachineState.IN_CHECK_SELF -> "正在自检中"
                MachineState.IN_GIVE_BACK -> "正在归还手环"
                MachineState.IN_SINGLE_GIVE_BACK -> "正在单步归还手环"
                MachineState.TEST -> "正在测试连通性"
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
        initProcessor.OnMsg(msg, data)
        fetchProcessor.OnMsg(msg, data)
        if (machineState == MachineState.IN_GIVE_BACK) {
            givebackProcessor.OnMsg(msg, data)
        } else {
            singleGiveBackProcessor.OnMsg(msg, data)
        }
        testProcessor.OnMsg(msg, data)
        robotListener?.OnMsg(msg, data)
    }
}