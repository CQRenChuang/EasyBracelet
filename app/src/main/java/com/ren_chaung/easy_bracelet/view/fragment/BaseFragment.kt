package com.ren_chaung.easy_bracelet.view.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity

/**
 * 基础Fragment，都加上手环机回调监听
 */
open class BaseFragment : Fragment(), BraceletMachineListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    open fun onBack() {}

    fun push(fragment: Fragment) {
        (activity as? BaseFragmentActivity)?.push(fragment)
    }

    fun popFragment() {
        (activity as? BaseFragmentActivity)?.popFragment()
    }


    fun popFragment(num: Int) {
        (activity as? BaseFragmentActivity)?.popFragment(num)
    }
}
