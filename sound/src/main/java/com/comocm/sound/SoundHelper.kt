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
        sp.load(context, R.raw.nobrand, 1)//0
        sp.load(context, R.raw.recivetip, 1)//1
        sp.load(context, R.raw.recivesuccess, 1)//2
        sp.load(context, R.raw.begin_fetch, 1)//3
        sp.load(context, R.raw.end_fetch, 1)//4
        sp.load(context, R.raw.retry, 1)//5
        sp.load(context, R.raw.exception_card, 1)//6
    }

    private fun play(index: Int) {
        if (index >= putSound.size) return
        try {
            sp.play(putSound[index], 1f, 1f, 5, 0, 1f)
        Log.i(TAG, "play: $index")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        play(6)
    }

    fun pleaseRetry() {
        writeLog("语音: 请重试")
        play(5)
    }

    fun endFetch() {
        writeLog("语音: 您的手环发放完毕，谢谢使用")
        play(4)
    }

    fun beginFetch() {
        writeLog("语音: 请稍等，正在发放手环")
        play(3)
    }

    //TODO 无手环 请联系管理员
    fun noBrand() {
        writeLog("语音: 没有手环")
        play(0)
    }

    //TODO 请将手环放至回收口
    fun reciveTip() {
        writeLog("语音:请将手环放至回收口")
        play(1)
    }

    //TODO 归还成功
    fun reciveSuccess() {
        lastreciveTime = System.currentTimeMillis()
        writeLog("语音:归还成功")
        play(2)
    }

    fun writeLog(msg: String) {
        OCMLogger.write(msg)
    }
}