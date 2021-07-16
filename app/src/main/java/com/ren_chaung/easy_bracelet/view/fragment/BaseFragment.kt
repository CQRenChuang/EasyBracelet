package com.ren_chaung.easy_bracelet.view.fragment

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import kotlinx.android.synthetic.main.activity_base_fragment.*
import java.util.*
import kotlin.concurrent.timer

/**
 * 基础Fragment，都加上手环机回调监听
 */
open class BaseFragment : Fragment(), BraceletMachineListener {

    private var title: String = ""
    private val handler = Handler()
    private var toastTimer: Timer? = null

    fun setTitle(title: String) {
        (activity as? BaseFragmentActivity)?.title = title
        this.title = title
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    open fun onBack() {}

    open fun canGoBack(): Boolean {
        return true
    }

    open fun onReShow() {
        (activity as? BaseFragmentActivity)?.title = title
    }

    fun push(fragment: BaseFragment) {
        (activity as? BaseFragmentActivity)?.push(fragment)
    }

    fun popFragment() {
        (activity as? BaseFragmentActivity)?.apply {
            popFragment()
        }
    }


    fun popFragment(num: Int) {
        (activity as? BaseFragmentActivity)?.popFragment(num)
    }

    fun showToast(text: String?) {
        if (text == null || text.isEmpty()) return
        handler.post {
            (activity as? BaseFragmentActivity)?.let {
                it.tvToast?.text = text
                it.tvToast?.visibility = View.VISIBLE
                toastTimer?.cancel()
                toastTimer = timer(initialDelay = 2000, period = 2000) {
                    handler.post {
                        it.tvToast?.visibility = View.INVISIBLE
                    }
                    toastTimer?.cancel()
                }
            }
        }
    }
}
