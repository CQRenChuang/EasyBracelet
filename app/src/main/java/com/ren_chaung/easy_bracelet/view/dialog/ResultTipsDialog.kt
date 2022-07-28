package com.ren_chaung.easy_bracelet.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.WindowManager
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.LocalLogger
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import kotlinx.android.synthetic.main.dialog_result_tips.*
import java.util.*
import kotlin.concurrent.timer


class ResultTipsDialog private constructor(context: Context, themeStyle: Int) : Dialog(context, themeStyle) {

    var state: Builder.State = Builder.State.SUCCESS
        set(value) {
            when (value) {
                Builder.State.SUCCESS -> {
                    ivStatus.setBackgroundResource(R.mipmap.gif_dialog_sucess)
                    tvState.text = tips ?: "成功"
                }
                Builder.State.FAIL -> {
                    ivStatus.setBackgroundResource(R.mipmap.gif_dialog_fail)
                    tvState.text = tips ?: "失败"
                }
                Builder.State.ERROR -> {
                    ivStatus.setBackgroundResource(R.mipmap.ic_error)
                    tvState.text = tips ?: "错误"
                }
            }
            field = value
        }

    var tips: String? = null
        set(value) {
            tvState.text = value ?: ""
            field = value
        }
    private var timer: Timer? = null
    private var buttonText = ""
    private var countdown = 0
    private val handler = Handler()

    init {
        setupView()
    }

    private fun setupView() {
        setContentView(R.layout.dialog_result_tips)
    }

    fun show(delayDismiss: Long = 3000, onDismiss: (()->Unit)) {
        show()
        dismiss(delayDismiss, onDismiss)
    }

    fun dismiss(delay: Long, onDismiss: (()->Unit) = {}) {
        setOnDismissListener {
            onDismiss()
            timer?.cancel()
            timer = null
        }
        buttonText = buttonDone.text.toString()
        buttonDone?.text = "$buttonText（${(delay - countdown)/1000}）"
        buttonDone.visibility = View.VISIBLE
        timer = timer(period = 1000, initialDelay = 1000) {
            handler.post {
                buttonDone?.text = "$buttonText（${(delay - countdown)/1000}）"
            }
            countdown += 1000
            if (countdown > delay) {
                timer?.cancel()
                timer = null
                handler.post {
                    if (isShowing) {
                        dismiss()
                    }
                }
            }
        }
    }

    override fun dismiss() {
        LocalLogger.write("dismissDialog： ${tips ?: ""}")
        super.dismiss()
    }

    override fun show() {
        window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        LocalLogger.write("showDialog： ${tips ?: ""}")
        super.show()
        val decorView = window?.decorView
        decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    class Builder (private val context: Context, private val state: State, private val tips: String? = null) {
        enum class State {
            FAIL, ERROR, SUCCESS
        }

        fun create(): ResultTipsDialog {
            /**
             * 利用我们刚才自定义的样式初始化Dialog
             */
            val dialog = ResultTipsDialog(context,
                R.style.noFrameDialogMask)
            dialog.tips = tips
            dialog.state = state
            dialog.buttonDone.setOnSingleClickListener {
                dialog.dismiss()
            }
            /**
             * 禁止点击Dialog以外的区域时Dialog消失
             */
            dialog.setCanceledOnTouchOutside(false)
            dialog.window?.setWindowAnimations(R.style.dialogWindowAnim)
            return dialog
        }
    }
}