package com.ocm.bracelet_machine_sdk

/**
 * 手环机器回调
 * 可选实现
 */
interface BraceletMachineListener {

    /**
     * 设备断开连接
     */
    fun onDisconnect() {}

    /**
     * 当剩余手环数量
     * @param num Int 剩余数量
     */
    fun onCurrentNumChange(num: Int) {}

    /**
     * 机器运转状态改变
     * @param isStop Boolean 是否停止，停止后需要设置当前数量来重新启动
     */
    fun onStateChange(isStop: Boolean){}
}

/**
 * 手环机状态回调
 */
interface CheckStatusCallback {
    /**
     * 成功
     * @param status BraceletMachineManager.BraceletMachineStatus 当前状态
     */
    fun onSuccess(status: BraceletMachineManager.BraceletMachineStatus) {}

    /**
     * 失败
     * @param msg String 失败原因
     */
    fun onFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

/**
 * 默认通用回调
 */
interface DefaultCallback {
    /**
     * 成功
     * @param msg String
     */
    fun onSuccess(msg: String) {}

    /**
     * 失败
     * @param msg String 失败原因
     */
    fun onFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}


/**
 * 自检回调
 */
interface CheckSelfCallback {
    /**
     * 自检成功
     */
    fun onCheckSelfSuccess() {}

    /**
     * 自检失败
     * @param msg String 失败原因
     */
    fun onCheckSelfFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

/**
 * 获取手环回调
 */
interface FetchCallback {
    /**
     * 获取手环成功
     * @param no String 对应号码
     */
    fun onFetchSuccess(no: String) {}

    /**
     * 取多个手环时还剩余要取几个
     * @param num String 剩余要取数
     */
    fun onRemainingFetch(num: Int) {}

    /**
     * 获取手环失败
     * @param msg String 提示文字
     */
    fun onFetchFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

/**
 * 还手环回调
 */
interface GiveBackCallback {
    /**
     * 是否允许归还手环，需要验证手环是否合法
     * @param card CardDataModel 手环信息
     * @param callback CheckGiveBackCallback 默认允许，调用allow方法以验证
     */
    fun checkAllowGiveBack(card: com.ocm.bracelet_machine_sdk.Machine.CardDataModel, callback: com.ocm.bracelet_machine_sdk.CheckGiveBackCallback) { callback.allow(true, "") }

    /**
     * 还手环忙碌提醒
     */
    fun onGiveBackBusy() {}

    /**
     * 还手环成功
     * @param no String 手环号
     */
    fun onGiveBackSuccess(no: String) {}

    /**
     * 还手环失败
     * @param msg String 失败原因
     */
    fun onGiveBackFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

interface WRGiveBackCallback {

    fun onSuccess(card: com.ocm.bracelet_machine_sdk.Machine.CardDataModel)

    /**
     * 还手环失败
     * @param msg String 失败原因
     */
    fun onGiveBackFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

interface AllowGiveBackCallback {
    /**
     * 还手环成功
     */
    fun onSuccess() {}

    /**
     * 还手环失败
     * @param msg String 失败原因
     */
    fun onFail(msg: String) {}

    /**
     * 执行完成
     */
    fun onCompleted() {}
}

/**
 * 手环机系统回调
 */
interface BraceletMachineSystemListener {
    fun onSuccess()
    fun onFail()
}


/**
 *  校验是否能还手环
 */
interface CheckGiveBackCallback {
    /**
     * 是否同意归还
     * @param checkResult Boolean true 则同意
     * @param msg String 如果拒绝，提示信息内容
     */
    fun allow(checkResult: Boolean, msg: String)
}