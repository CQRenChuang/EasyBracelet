package com.ren_chaung.easy_bracelet.view.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.bracelet_machine_sdk.utils.LocalLogger
import com.ren_chaung.easy_bracelet.BuildConfig
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.receiver.TimeTickReceiver
import com.ren_chaung.easy_bracelet.utils.DateTimeHelper
import com.ren_chaung.easy_bracelet.utils.extension.setOnSingleClickListener
import com.ren_chaung.easy_bracelet.view.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_base_fragment.*
import java.util.*
import kotlin.collections.ArrayList

open class BaseFragmentActivity: FragmentActivity() {

    protected enum class FragmentAnimateType {
        FADE, SLIDE_LEFT, SLIDE_RIGHT
    }
    val fragments = ArrayList<BaseFragment>()
    private var lastPushTime = 0L
    private var lastPopTime = 0L
    private var fragmentContainerId = 0
    private lateinit var receiver: BroadcastReceiver
    private val timeTickReceiver = TimeTickReceiver()

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        tvTitle.text = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base_fragment)
        fragmentContainerId = layoutContainer.id
        ivBack.visibility = View.GONE
        ivBack.setOnSingleClickListener {
            backAction()
        }
        val type = if(BraceletMachineManager.isIC()) "IC" else "ID"
        tvVersion.text = "v${BuildConfig.VERSION_NAME}_${type}"//resources.getString(R.string.app_version, "${}${if (EquipmentManager.isDemo) ".dev" else ""}")
        tvTime.text = DateTimeHelper.getTime()
        setupReceiver()
//        val decorView = window.decorView
//// Hide both the navigation bar and the status bar.
//// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//// a general rule, you should design your app to hide the status bar whenever you
//// hide the navigation bar.
//// Hide both the navigation bar and the status bar.
//// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//// a general rule, you should design your app to hide the status bar whenever you
//// hide the navigation bar.
//        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN)
//        decorView.systemUiVisibility = uiOptions
    }

    //保持字体
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale < 1.3f) {
            getResources()
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        if (res.configuration.fontScale < 1.3f) {
            val newConfig = Configuration()
            newConfig.fontScale = 1.3F
            res.updateConfiguration(newConfig, res.displayMetrics)
        }
        return res
    }

    //监听广播
    private fun setupReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    when(it.action) {
                        else -> {}
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        registerReceiver(receiver, intentFilter)
        val timeFilter = IntentFilter()
        timeFilter.addAction(Intent.ACTION_TIME_TICK)
        registerReceiver(timeTickReceiver, timeFilter)
    }

    open fun backAction() {
        if (fragments.size < 2) {
            finish()
        } else {
            popFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkPermission()) {
            requestPermission()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        unregisterReceiver(timeTickReceiver)
        super.onDestroy()
    }

    override fun setContentView(layoutResID: Int) {
        LayoutInflater.from(this).inflate(layoutResID, layoutContainer, true)
    }

    protected fun setFragmentContainer(id: Int) {
        fragmentContainerId = id
    }

    //fragment 相关
    open fun push(fragment: BaseFragment) {
        val nowTime = Date().time
        if (nowTime - lastPushTime <= 1000) return
                lastPushTime = nowTime
        val lastFragment = fragments[fragments.size - 1]
        fragments.add(fragment)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out)
        transaction.hide(lastFragment)
        transaction.add(fragmentContainerId, fragment).commitAllowingStateLoss()
        LocalLogger.write("push(fragment: ${fragment::class.java.simpleName}")
        ivBack.visibility = if(fragments.size > 1) View.VISIBLE else View.GONE
    }

    open fun popFragment() {
        popFragment(1)
    }

    open fun popFragment(num: Int) {
        if (fragments.size < 2) {
            return
        }
        val nowTime = Date().time
//        if (nowTime - lastPopTime <= 1000) return
        lastPopTime = nowTime
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out)
        for (i in 1..num) {
            fragments.getOrNull(fragments.size - 1)?.let { fragment ->
                if (!fragment.canGoBack()) return
                fragments.remove(fragment)
                fragment.onBack()
                transaction.remove(fragment)
                LocalLogger.write("popFragment: ${fragment::class.java.simpleName}")
            }
            if (fragments.size < 2) {
                break
            }
        }
        val showFragment = fragments[fragments.size - 1]
        showFragment.onReShow()
        transaction.show(showFragment).commitAllowingStateLoss()
        ivBack.visibility = if(fragments.size > 1) View.VISIBLE else View.GONE
    }

    protected fun replaceFragment(fragment: BaseFragment, animateType: FragmentAnimateType) {
        fragments.removeAll { true }
        fragments.add(fragment)
        LocalLogger.write("replaceFragment: ${fragment::class.java.simpleName}")
        val transaction = supportFragmentManager.beginTransaction()
        when(animateType) {
            FragmentAnimateType.FADE ->
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            FragmentAnimateType.SLIDE_LEFT ->
                transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out)
            FragmentAnimateType.SLIDE_RIGHT ->
                transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out)
        }
        transaction.replace(fragmentContainerId, fragment).commitAllowingStateLoss()
    }

    //没有网络
    protected fun noNet() {
//        val netErrorDialog = ResultTipsDialog.Builder(this, ResultTipsDialog.Builder.State.FAIL, "网络异常，请设置网络").create()
//        netErrorDialog.buttonDone?.visibility = View.VISIBLE
//        netErrorDialog.buttonDone?.text = resources.getString(R.string.setting)
//        netErrorDialog.buttonDone?.setOnClickListener {
//            startActivity(Intent(Settings.ACTION_SETTINGS))
//            netErrorDialog.dismiss()
//        }
//        netErrorDialog.show()
//        isCanPush = true
    }

    //权限
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 3)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (!checkPermission()) {
                requestPermission()
            }
        }
    }
}