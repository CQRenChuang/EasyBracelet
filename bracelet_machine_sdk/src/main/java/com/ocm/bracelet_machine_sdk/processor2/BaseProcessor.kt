package com.ocm.bracelet_machine_sdk.processor2

import com.ocm.bracelet_machine_sdk.Machine.RobotInterface
import com.ocm.bracelet_machine_sdk.Machine.RobotMsg
import com.ocm.bracelet_machine_sdk.utils.LocalLogger


//消息处理接收
internal open class BaseProcessor : RobotInterface {
    override fun OnMsg(msg: RobotMsg?, data: Any?) {
        LocalLogger.write("${this.javaClass.simpleName}: msg=${msg}")
    }
}