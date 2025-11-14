package com.dongxingx.logger.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object CMDHelper {
    fun reboot() {
        shellExec("reboot")
    }

    fun shellExec(cmd: String) {
        val mRuntime = Runtime.getRuntime()
        try {
            //Process中封装了返回的结果和执行错误的结果
            val mProcess = mRuntime.exec(cmd)
            val mReader = BufferedReader(InputStreamReader(mProcess.inputStream))
            val mRespBuff = StringBuffer()
            var line: String? = null
            while ({ line = mReader.readLine(); line }() != null) {
                mRespBuff.append(line)
            }
            mReader.close()
            print(mRespBuff.toString())
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}