package com.ren_chaung.easy_bracelet.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.WindowManager
import com.comocm.base.extension.showToast
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import kotlinx.android.synthetic.main.dialog_setup_num.*


class SetupNumDialog private constructor(context: Context, themeStyle: Int) : Dialog(context, themeStyle) {

    private val handler = Handler()

    init {
        setupView()
    }

    private fun setupView() {
        setContentView(R.layout.dialog_setup_num)
    }

    override fun dismiss() {
        LocalLogger.write("dismissDialog： SetupNumDialog")
        super.dismiss()
    }

    override fun show() {
        window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        LocalLogger.write("showDialog：SetupNumDialog")
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

    companion object {
        fun create(context: Context): SetupNumDialog {
            /**
             * 利用我们刚才自定义的样式初始化Dialog
             */
            return SetupNumDialog(context,
                R.style.noFrameDialogMask).apply {
                etMaxNum.setText(BraceletMachineManager.maxBracelet.toString())
                etCurrentNum.setText(BraceletMachineManager.currentBracelet.toString())
                buttonCancel.setOnSingleClickListener {
                    dismiss()
                }
                buttonSave.setOnSingleClickListener {
                    val maxStr = etMaxNum.text.toString()
                    if (maxStr.isEmpty()) {
                        context.showToast(context.getString(R.string.please_set_max))
                        return@setOnSingleClickListener
                    }
                    val currentStr = etCurrentNum.text.toString()
                    if (currentStr.isEmpty()) {
                        context.showToast(context.getString(R.string.please_set_current))
                        return@setOnSingleClickListener
                    }
                    val max = maxStr.toIntOrNull() ?: 120
                    val current = currentStr.toIntOrNull() ?: 0
                    if (max > 120) {
                        context.showToast(context.getString(R.string.limit_tips))
                        return@setOnSingleClickListener
                    }
                    if (current > max) {
                        context.showToast(context.getString(R.string.more_than_max))
                        return@setOnSingleClickListener
                    }
                    BraceletMachineManager.maxBracelet = max
                    BraceletMachineManager.currentBracelet = current
                    if (current > 0)
                        BraceletMachineManager.start()
                    dismiss()
                }
                //禁止点击Dialog以外的区域时Dialog消失
                setCanceledOnTouchOutside(false)
                window?.setWindowAnimations(R.style.dialogWindowAnim)
            }
        }
    }
}