package com.ocm.bracelet_machine_sdk.processor

import com.ocm.bracelet_machine_sdk.BuildConfig
import com.ocm.bracelet_machine_sdk.Machine.RobotInterface
import com.ocm.bracelet_machine_sdk.Machine.RobotMsg
import com.ocm.bracelet_machine_sdk.utils.LocalLogger


//消息处理接收
internal open class BaseProcessor : RobotInterface {
    override fun OnMsg(msg: RobotMsg?, data: Any?) {
        BuildConfig.BUILD_TYPE
        LocalLogger.write("${BuildConfig.VERSION_NAME}-${this.javaClass.simpleName}: msg=${msg}")
    }
}