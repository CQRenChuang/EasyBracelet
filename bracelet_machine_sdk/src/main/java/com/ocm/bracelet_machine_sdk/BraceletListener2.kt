 package com.ocm.bracelet_machine_sdk

/**
 * 手环机器回调
 * 可选实现
 */
interface BraceletListener {

    /**
     * 设备断开连接
     */
    fun onDisconnect() {}

    /**
     * 当剩余手环数量
     * @param num Int 剩余数量
     */
    fun onCurrentNumChange(addr: Int, num: Int) {}

    /**
     * 机器运转状态改变
     * @param isStop Boolean 是否停止，停止后需要设置当前数量来重新启动
     */
    fun onStateChange(addr: Int, isStop: Boolean){}
}
