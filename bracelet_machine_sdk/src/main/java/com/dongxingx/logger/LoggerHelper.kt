package com.dongxingx.logger

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.dongxingx.logger.utils.ZipUtil
import floatwindow.xishuang.float_lib.FloatLoger
import java.io.File
import java.util.Date
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
object LoggerHelper {
    var context: Context? = null
    private var qiniuToken = ""
    private const val TAG = "dx-logger"
    private var isInit = false
    private var isAuthCheck = false
    private val zipUtil = ZipUtil()
    private var isUploadLog = false
    private var logPath = ""
    private val baseUrl = "https://cgy-upload.dongxingx.cn"

    private val threadPoolExecutor = ThreadPoolExecutor(
        1, 1, 1, TimeUnit.SECONDS,
        LinkedBlockingQueue(10),
    )

    fun setup(context: Context) {
        if (isInit) return
        isInit = true
        this.context = context
        logPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${context.getExternalFilesDir("")?.absolutePath}/LocalLoger/"
        } else {
            "${Environment.getExternalStorageDirectory().absolutePath}/LocalLoger/"
        }
        FloatLoger.ShowFloat = false
        FloatLoger.setSize(500*1024)
        FloatLoger.getInstance().startServer(context)
        FloatLoger.getInstance().hide()
        FloatLoger.getInstance().clearOutLog(5)
    }

    fun write(log: String, isWrite: Boolean = true) {
        Log.d(TAG, log)
        if (isWrite && isAuthCheck) FloatLoger.getInstance().writeLog(log)
    }

    fun upload(
        productName: String,
        name: String,
        completed: (Result<String>) -> Unit) {
        completed(Result.failure(Throwable("不支持SDK上传，请自行找 $logPath")))
        return
        if (isUploadLog) {
        }
        isUploadLog = true
        if (qiniuToken.isEmpty()) {
//            LoggerApi.getQiNiuToken(null) {
//                it.onSuccess {  token ->
//                    qiniuToken = token
//                    toUpload(productName, name, completed)
//                }
//                it.onFailure {ex ->
//                    isUploadLog = false
//                    completed(Result.failure(Exception("获取token失败：${ex}")))
//                }
//            }
        } else {
            toUpload(productName, name, completed)
        }
    }

    private fun toUpload(
        productName: String,
        name: String,
        completed: (Result<String>) -> Unit) {
        completed.invoke(Result.failure(Throwable("不支持SDK上传，请自行找 $logPath")))
        return
        try {
            threadPoolExecutor.execute {
                val dir = File(logPath)
                if (!dir.exists()) {
                    isUploadLog = false
                    completed(Result.failure(Exception("没有日志目录")))
                    return@execute
                }
                val fileList =
                    dir.listFiles { file, filename -> return@listFiles filename.contains(name) }
                if (fileList.isNullOrEmpty()) {
                    isUploadLog = false
                    completed(Result.failure(Exception("没有$name 日志文件")))
                    return@execute
                }
//                val mac = context?.let { NetHelper.getLocalMac(it) }
//                val uploadPath = "android_log/${mac}/${productName}/"
//                val uploadFile: File = if (fileList.size > 1) {
//                    val zipLogPath = "${logPath}${name}.zip"
//                    zipUtil.zip(fileList.toList(), zipLogPath)
//                    File(zipLogPath)
//                } else {
//                    fileList.first()
//                }
//                val awsFileName = "$uploadPath${Date().time}_${uploadFile.name}"
//                if (uploadFile.exists()) {
//                    val stringBuilder = StringBuilder()
//                    UploadManager().put(uploadFile, awsFileName, qiniuToken,
//                        { key, info, res -> //res 包含 hash、key 等信息，具体字段取决于上传策略的设置
//                            stringBuilder.append("** $productName **\n")
//                            stringBuilder.append("设备: $mac\n")
//                            if (info.isOK) {
//                                val url = "${baseUrl}/${awsFileName}"
//                                if (awsFileName.contains(".zip")) {
//                                    uploadFile.delete()
//                                }
//                                Log.i("qiniu", "Upload Success - $url")
//                                stringBuilder.append("日志上传成功: [下载]($url) $url")
//                                isUploadLog = false
//                                completed(Result.success(url))
//                                LoggerApi.sendQywx(stringBuilder.toString())
//                            } else {
//                                Log.i("qiniu", "七牛上传日志失败 - ${info.error}")
//                                //如果失败，这里可以把 info 信息上报自己的服务器，便于后面分析上传错误原因
//                                qiniuToken = ""
//                                isUploadLog = false
//                                completed(Result.failure(Exception(info.error)))
//                            }
//                            Log.i("qiniu", "$key,\r\n $info,\r\n $res")
//                        },
//                        UploadOptions(null, null, false,
//                            { key, percent -> Log.i("qiniu", "$key: $percent") }, null
//                        )
//                    )
//                } else {
//                    isUploadLog = false
//                    val tip = "日志不存在：${uploadFile.path}"
//                    completed(Result.failure(Exception(tip)))
//                    Log.i("uploadImg", tip)
//                }
            }

        } catch (ex: Throwable) {
            isUploadLog = false
            ex.printStackTrace()
        }
    }
}