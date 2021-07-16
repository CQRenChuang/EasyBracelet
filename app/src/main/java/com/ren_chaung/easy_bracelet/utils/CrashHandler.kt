package com.ren_chaung.easy_bracelet.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import com.tencent.bugly.crashreport.CrashReport
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler(private val mContext: Context?) : Thread.UncaughtExceptionHandler {

    companion object {
        const val TAG = "CrashHandler"

        fun init(mContext: Context?) {
            CrashHandler(mContext).setup()
        }
    }

    private val PATH =
        Environment.getExternalStorageDirectory().path + "/LocalLoger/"
    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    // 用来存储设备信息和异常信息
    private val infos: MutableMap<String, String> =
        HashMap()
    // 用于格式化日期,作为日志文件名的一部分
    private val formatter: DateFormat = SimpleDateFormat("yyyyMMdd")

    /**
     * 初始化
     *
     * @param context
     */
    fun setup() {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(
        thread: Thread,
        ex: Throwable
    ) {
        if (!handleException(ex) && mDefaultHandler != null) { // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                Log.e(TAG, "error : ", e)
            }
            // 退出程序
            Process.killProcess(Process.myPid())
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        CrashReport.postCatchedException(ex)
        ex.printStackTrace()
        // 使用Toast来显示异常信息
        object : Thread() {
            override fun run() {
                Looper.prepare()
                Toast.makeText(mContext, "很抱歉,程序出现异常即将退出.", Toast.LENGTH_LONG).show()
                Looper.loop()
            }
        }.start()
        // 收集设备参数信息
        collectDeviceInfo(mContext)
        // 保存日志文件
        saveCrashInfo2File(ex)
        return true
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.packageManager
            val pi =
                pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName =
                    if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                TAG,
                "an error occured when collect package info",
                e
            )
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field[null].toString()
                Log.d(
                    TAG,
                    field.name + " : " + field[null]
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "an error occured when collect crash info",
                    e
                )
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfo2File(ex: Throwable): String? {
        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            val time = formatter.format(Date())
            val fileName = time + "crash.log"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val dir = File(PATH)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val fos =
                    FileOutputStream(PATH + fileName)
                fos.write(sb.toString().toByteArray(charset("UTF-8")))
                fos.close()
            }
            return fileName
        } catch (e: Exception) {
            Log.e(
                TAG,
                "an error occured while writing file...",
                e
            )
        }
        return null
    }

}