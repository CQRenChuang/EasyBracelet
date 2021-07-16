package com.comocm.sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.ocm.ocmlogger.OCMLogger
import java.lang.ref.WeakReference
import java.util.*

/**
 * 声音播放助手
 * 使用前先载入声音 loadSound
 */
object SoundHelper {
    private lateinit var sp: SoundPool
    private lateinit var putSound: MutableList<Int>


    private var TAG = SoundHelper::class.java.simpleName
    //TODO 正在比对
    private var detectingTime: Long = 0
    private var lastreciveTime: Long = 0

    private var lastmaletime: Long = 0
    private var weakContext: WeakReference<Context>? = null
    private var preparePlays = ArrayList<Int>()
    private var isPlaying = false

    fun loadSound(context: Context) {
        weakContext = WeakReference(context)
        putSound = ArrayList()
        sp = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        sp.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Log.i("SoundPool", "音效加载完成:$sampleId")
            putSound.add(sampleId)
        }

        sp.load(context, R.raw.changeposition, 1)//0
        sp.load(context, R.raw.putingbrand, 1)//1
        sp.load(context, R.raw.takebrand, 1)//2
        sp.load(context, R.raw.nobrand, 1)//3
        sp.load(context, R.raw.detecting, 1)//4
        sp.load(context, R.raw.matchsuccess, 1)//5
        sp.load(context, R.raw.matchfail, 1)//6
        sp.load(context, R.raw.recivetip, 1)//7
        sp.load(context, R.raw.recivesuccess, 1)//8
        sp.load(context, R.raw.contractadmin, 1)//9
        sp.load(context, R.raw.plasetakeyourgood, 1)//10
        sp.load(context, R.raw.disconnect, 1)//11
        sp.load(context, R.raw.maleuse, 1)//12
        sp.load(context, R.raw.femaleuse, 1)//13
        sp.load(context, R.raw.wating, 1)//14
        sp.load(context, R.raw.confirm, 1)//15
        sp.load(context, R.raw.adjust_the_position, 1)//16
        sp.load(context, R.raw.qr_scan_tips, 1)//17
        sp.load(context, R.raw.guidance, 1)//18
        sp.load(context, R.raw.help, 1)//19
        sp.load(context, R.raw.input_mobile, 1)//20
        sp.load(context, R.raw.qr_camera_tips, 1)//21
        sp.load(context, R.raw.guidance_no_qr, 1)//22
        sp.load(context, R.raw.begin_fetch, 1)//23
        sp.load(context, R.raw.take_1, 1)//24
        sp.load(context, R.raw.take_2, 1)//25
        sp.load(context, R.raw.take_3, 1)//26
        sp.load(context, R.raw.take_4, 1)//27
        sp.load(context, R.raw.end_fetch, 1)//28
        sp.load(context, R.raw.selectcoachandclass, 1)//29
        sp.load(context, R.raw.confirmstart, 1)//30
        sp.load(context, R.raw.confirmend, 1)//31
        sp.load(context, R.raw.confirmcancel, 1)//32
        sp.load(context, R.raw.coachokvalidmember, 1)//33
        sp.load(context, R.raw.startsuccess, 1)//34
        sp.load(context, R.raw.endsuccess, 1)//35
        sp.load(context, R.raw.cancelsuccess, 1)//36
        sp.load(context, R.raw.inbox, 1)//37
        sp.load(context, R.raw.enter_face_check_info, 1)//38
        sp.load(context, R.raw.enter_face_take_success, 1)//39
        sp.load(context, R.raw.enter_face_take_tips, 1)//40
        sp.load(context, R.raw.pic_uploading, 1)//41
        sp.load(context, R.raw.upload_success_back_home, 1)//42
        sp.load(context, R.raw.retry, 1)//43
        sp.load(context, R.raw.exception_card, 1)//44
    }

    private fun play(index: Int) {
        if (index >= putSound.size) return
        try {
            sp.play(putSound[index], 1f, 1f, 5, 0, 1f)
        Log.i(TAG, "play: $index")
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        when(index) {
//            0 -> preparePlays.add(R.raw.changeposition)
//            1 -> preparePlays.add(R.raw.putingbrand)
//            2 -> preparePlays.add(R.raw.takebrand)
//            3 -> preparePlays.add(R.raw.nobrand)
//            4 -> preparePlays.add(R.raw.detecting)
//            5 -> preparePlays.add(R.raw.matchsuccess)
//            6 -> preparePlays.add(R.raw.matchfail)
//            7 -> preparePlays.add(R.raw.recivetip)
//            8 -> preparePlays.add(R.raw.recivesuccess)
//            9 -> preparePlays.add(R.raw.contractadmin)
//            10 -> preparePlays.add(R.raw.plasetakeyourgood)
//            11 -> preparePlays.add(R.raw.disconnect)
//            12 -> preparePlays.add(R.raw.maleuse)
//            13 -> preparePlays.add(R.raw.femaleuse)
//            14 -> preparePlays.add(R.raw.wating)
//            15 -> preparePlays.add(R.raw.confirm)
//            16 -> preparePlays.add(R.raw.adjust_the_position)
//            17 -> preparePlays.add(R.raw.qr_scan_tips)
//            18 -> preparePlays.add(R.raw.guidance)
//            19 -> preparePlays.add(R.raw.help)
//            20 -> preparePlays.add(R.raw.input_mobile)
//            21 -> preparePlays.add(R.raw.qr_camera_tips)
//            22 -> preparePlays.add(R.raw.guidance_no_qr)
//            23 -> preparePlays.add(R.raw.begin_fetch)
//            24 -> preparePlays.add(R.raw.take_1)
//            25 -> preparePlays.add(R.raw.take_2)
//            26 -> preparePlays.add(R.raw.take_3)
//            27 -> preparePlays.add(R.raw.take_4)
//            28 -> preparePlays.add(R.raw.end_fetch)
//            else -> return
//        }
//        if (isPlaying) return
//        isPlaying = true
//        beginPlay()
    }

    private fun beginPlay() {
        val firstId = preparePlays.firstOrNull()
        if (firstId == null) {
            isPlaying = false
            return
        }
        playMedia(firstId)
    }

    private fun playMedia(id: Int) {
        weakContext?.get()?.let {
            try {
                val mediaPlayer = MediaPlayer.create(it, id)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.stop()
                    mp.release()
                    preparePlays.removeAt(0)
                    beginPlay()
                }
                mediaPlayer.setOnPreparedListener { mp ->
                    mp.start()
                }
                mediaPlayer.prepare()
                Log.i(TAG, "playMedia: $id")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                writeLog("语音错误: ${e.stackTrace}")
            }
        }
    }

    fun exceptionCard() {
        writeLog("语音:未读到卡号，请核对")
        play(44)
    }

    fun pleaseRetry() {
        writeLog("语音: 请重试")
        play(43)
    }

    fun uploadSuccessBackHome() {
        writeLog("语音: 上传成功，返回首页")
        play(42)
    }

    fun picUploading() {
        writeLog("语音: 照片上传中，请稍后⋯")
        play(41)
    }

    fun enterFaceTakeTips() {
        writeLog("语音: 请将脸正对摄像头，置于框内")
        play(40)
    }

    fun enterFaceTakeSuccess() {
        writeLog("语音: 拍照成功，请确认")
        play(39)
    }

    fun enterFaceCheckInfo() {
        writeLog("语音: 核对信息正确后，点击信息无误")
        play(38)
    }

    fun selectCoachAndClass() {
        writeLog("语音: 选择教练和课程")
        play(29)
    }

    fun confirmStart() {
        writeLog("语音: 确认上课信息")
        play(30)
    }

    fun confirmEnd() {
        writeLog("语音: 确认下课信息")
        play(31)
    }

    fun confirmCancel() {
        play(32)
    }

    fun coachOkValidMember() {
        writeLog("语音: 教练验证成功，验证会员")
        play(33)
    }

    fun startSuccess() {
        writeLog("语音: 上课成功")
        play(34)
    }

    fun endSuccess() {
        writeLog("语音: 下课成功")
        play(35)
    }

    fun cancelSuccess() {
        play(36)
    }

    fun inBox() {
        play(37)
    }

    fun endFetch() {
        writeLog("语音: 您的手环发放完毕，谢谢使用")
        play(28)
    }

    fun take4() {
        writeLog("语音: 请取走您的第四个手环")
        play(27)
    }

    fun take3() {
        writeLog("语音: 请取走您的第三个手环")
        play(26)
    }

    fun take2() {
        writeLog("语音: 请取走您的第二个手环")
        play(25)
    }

    fun take1() {
        writeLog("语音: 请取走您的第一个手环")
        play(24)
    }

    fun beginFetch() {
        writeLog("语音: 请稍等，正在发放手环")
        play(23)
    }

    //新手引导
    fun guidanceNoQR() {
        writeLog("语音: 新手引导没有二维码")
        play(22)
    }

    fun qrCameraTips() {
        writeLog("语音: 摄像头扫二维码")
        play(21)
    }

    fun inputMobile() {
        writeLog("语音: 请输入手机号")
        play(20)
    }

    //新手引导
    fun guidance() {
        writeLog("语音: 新手引导")
        play(18)
    }

    //帮助
    fun help() {
        writeLog("语音: 帮助")
        play(19)
    }

    //请在扫码框内识别二维码
    fun qrScanTips() {
        writeLog("语音: 扫码框内扫码")
        play(17)
    }

    //TODO 调整位置
    fun changePosition() {
        play(0)
    }

    //请调整位置
    fun adjustThePosition() {
        writeLog("语音: 调整二维码位置")
        play(16)
    }

    //TODO 正在放置手环
    fun putingBrand() {
        writeLog("语音: 正在放置手环")
        play(1)
    }

    //TODO 请取走手环
    fun takeBrand() {
        writeLog("语音: 请取走手环")
        play(2)
    }

    //TODO 无手环 请联系管理员
    fun noBrand() {
        writeLog("语音: 没有手环")
        play(3)
    }

    fun detecting() {
        if (System.currentTimeMillis() - detectingTime > 3000) {
            writeLog("语音:正在比对")
            detectingTime = System.currentTimeMillis()
            play(4)
        }
    }

    //TODO 验证成功
    fun matchSuccess() {
        writeLog("语音:验证成功")
        play(5)
    }

    //TODO 验证失败
    fun matchFail() {
        play(6)
    }

    //TODO 请将手环放至回收口
    fun reciveTip() {
        writeLog("语音:请将手环放至回收口")
        play(7)
    }

    //TODO 归还成功
    fun reciveSuccess() {
        lastreciveTime = System.currentTimeMillis()
        writeLog("语音:归还成功")
        play(8)
    }

    //TODO 归还手环失败 联系管理员
    fun contractAdmin() {
        if (System.currentTimeMillis() - lastreciveTime > 2000) {
            writeLog("语音:归还手环失败")
            play(9)
        }
    }

    fun takeGoods() {
        writeLog("语音:请取走您的物品")
        play(10)
    }

    fun disconnect() {
        play(11)
    }

    fun male() {
        if (System.currentTimeMillis() - lastmaletime > 3000) {
            lastmaletime = System.currentTimeMillis()
            play(12)
        }
    }

    fun female() {
        if (System.currentTimeMillis() - lastmaletime > 3000) {
            lastmaletime = System.currentTimeMillis()
            play(13)
        }
    }

    fun wating() {
        writeLog("语音:请稍等")
        play(14)
    }

    fun confirmPlease() {
        writeLog("语音:请确认您的信息")
        play(15)
    }

    fun writeLog(msg: String) {
        OCMLogger.write(msg)
    }
}