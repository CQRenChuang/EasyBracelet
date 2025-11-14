package com.dongxingx.logger.utils


import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * 执行shell脚本工具类
 * @author Mountain
 */
object CommandExecution {

    val TAG = "CommandExecution"

    val COMMAND_SU = "su"
    val COMMAND_SH = "sh"
    val COMMAND_EXIT = "exit\n"
    val COMMAND_LINE_END = "\n"

    /**
     * Command执行结果
     * @author Mountain
     */
    class CommandResult {
        var result = -1
        var errorMsg: String? = null
        var successMsg: String? = null
    }

    /**
     * 执行命令—单条
     * @param command
     * @param isRoot
     * @return
     */
    fun execCommand(command: String, isRoot: Boolean): CommandResult {
        val commands = arrayOf(command)
        return execCommand(commands, isRoot)
    }

    /**
     * 执行命令-多条
     * @param commands
     * @param isRoot
     * @return
     */
    fun execCommand(commands: Array<String>?, isRoot: Boolean): CommandResult {
        val commandResult = CommandResult()
        if (commands == null || commands.size == 0) return commandResult
        var process: Process? = null
        var os: DataOutputStream? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        var successMsg: StringBuilder? = null
        var errorMsg: StringBuilder? = null
        try {
            process = Runtime.getRuntime().exec(if (isRoot) COMMAND_SU else COMMAND_SH)
            os = DataOutputStream(process!!.outputStream)
            for (command in commands) {
                if (command != null) {
                    os.write(command.toByteArray())
                    os.writeBytes(COMMAND_LINE_END)
                    os.flush()
                }
            }
            os.writeBytes(COMMAND_EXIT)
            os.flush()
            commandResult.result = process.waitFor()
            //获取错误信息
            successMsg = StringBuilder()
            errorMsg = StringBuilder()
            successResult = BufferedReader(InputStreamReader(process.inputStream))
            errorResult = BufferedReader(InputStreamReader(process.errorStream))
            var s: String

//            while ({ line = successResult.readLine(); line }() != null) {
//                successMsg.append(buff, 0, ch)
//            }
//            while ({ line = errorResult.readLine(); line }() != null) {
//                errorMsg.append(buff, 0, ch)
//            }
            commandResult.successMsg = successMsg.toString()
            commandResult.errorMsg = errorMsg.toString()
            Log.i(
                TAG, commandResult.result.toString() + " | " + commandResult.successMsg
                        + " | " + commandResult.errorMsg
            )
        } catch (e: IOException) {
            val errmsg = e.message ?: ""
            if (errmsg != null) {
                Log.e(TAG, errmsg)
            } else {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            val errmsg = e.message
            if (errmsg != null) {
                Log.e(TAG, errmsg)
            } else {
                e.printStackTrace()
            }
        } finally {
            try {
                os?.close()
                successResult?.close()
                errorResult?.close()
            } catch (e: IOException) {
                val errmsg = e.message
                if (errmsg != null) {
                    Log.e(TAG, errmsg)
                } else {
                    e.printStackTrace()
                }
            }

            process?.destroy()
        }
        return commandResult
    }

}