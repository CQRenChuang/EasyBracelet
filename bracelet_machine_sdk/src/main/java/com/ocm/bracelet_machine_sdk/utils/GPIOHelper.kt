package com.ocm.smartrobot.utils

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.*
import kotlin.concurrent.timer

/**
 * 二维码模块处理
 */
internal object GPIOHelper {
    private const val LED_CTL_PATH = "/sys/class/zh_gpio_out/out"
    private const val TAG = "GPIOHelper"

    private enum class LedCTL(val param: String) {
        RED_ON("1"), RED_OFF("2"),
        LED_ON("5"), LED_OFF("6"),
        CAMERA_RED_ON("7"), CAMERA_RED_OFF("8"),
        RELAY_ON("9"), RELAY_OFF("10")
    }

    private var readQRTimer: Timer? = null

    fun startReadQR() {
        readQRTimer?.cancel()
        var isOn = false
        readQRTimer = timer(TAG, period = 1000) {
            isOn = !isOn
            ctlLed(if (isOn) LedCTL.RED_ON.param else LedCTL.RED_OFF.param)
        }
    }

    fun stopReadQR() {
        readQRTimer?.cancel()
        ctlLed(LedCTL.RED_OFF.param)
    }

    fun cameraRedOn() {
        ctlLed(LedCTL.CAMERA_RED_ON.param)
    }

    fun cameraRedOff() {
        ctlLed(LedCTL.CAMERA_RED_OFF.param)
    }

    fun ledOn() {
        ctlLed(LedCTL.LED_ON.param)
    }

    fun ledOff() {
        ctlLed(LedCTL.LED_OFF.param)
    }

    fun relayOn() {
        ctlLed(LedCTL.RELAY_ON.param)
    }

    fun relayOff() {
        ctlLed(LedCTL.RELAY_OFF.param)
    }

    private fun ctlLed(cmd: String) {
        val file = File(LED_CTL_PATH)
        if (!file.exists() || !file.canWrite()) {
            Log.e(TAG, "文件不存在 或者 不可写")
            return
        }
        try {
            val fout = FileOutputStream(file)
            val pWriter = PrintWriter(fout)
            pWriter.println(cmd)
            pWriter.flush()
            pWriter.close()
            fout.close()

        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}