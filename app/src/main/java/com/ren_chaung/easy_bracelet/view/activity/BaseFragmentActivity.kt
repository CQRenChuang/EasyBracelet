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
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.comocm.base.extension.setOnSingleClickListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ocm.ocmlogger.OCMLogger
import com.ren_chaung.easy_bracelet.BuildConfig
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_base_fragment.*
import java.util.*
import kotlin.collections.ArrayList

open class BaseFragmentActivity: FragmentActivity() {

    protected enum class FragmentAnimateType {
        FADE, SLIDE_LEFT, SLIDE_RIGHT
    }
    protected var fragments = ArrayList<Fragment>()
    private var lastPushTime = 0L
    private var lastPopTime = 0L
    private var fragmentContainerId = 0
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base_fragment)
        fragmentContainerId = layoutContainer.id
        ivBack.visibility = View.GONE
        ivBack.setOnSingleClickListener {
            backAction()
        }
//        tvVersion.setOnMultiClickListener(3) {
//            showToast(resources.getString(R.string.in_check_upgrade))
//            UpdateManager.checkUpdate(this, true)
//        }
        tvVersion.text = "v${BuildConfig.VERSION_NAME}"//resources.getString(R.string.app_version, "${}${if (EquipmentManager.isDemo) ".dev" else ""}")
        setupReceiver()
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
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermission()) {
            requestPermission()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
//        LayoutInflater.from(this).inflate(layoutResID, layoutContainer, true)
    }

    protected fun setFragmentContainer(id: Int) {
        fragmentContainerId = id
    }

    //fragment 相关
    open fun push(fragment: Fragment) {
        val nowTime = Date().time
        if (nowTime - lastPushTime <= 1000) return
                lastPushTime = nowTime
        val lastFragment = fragments[fragments.size-1]
        fragments.add(fragment)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out)
        transaction.hide(lastFragment)
        transaction.add(fragmentContainerId, fragment).commitAllowingStateLoss()
        OCMLogger.write("push(fragment: ${fragment::class.java.simpleName}")
//        layoutBack.visibility = if(fragments.size > 1) View.VISIBLE else View.INVISIBLE
    }

    open fun popFragment() {
        popFragment(1)
    }

    open fun popFragment(num: Int) {
        if (fragments.size < 2) {
            return
        }
        val nowTime = Date().time
        if (nowTime - lastPopTime <= 1000) return
        lastPopTime = nowTime
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out)
        for (i in 1..num) {
            (fragments.getOrNull(fragments.size-1) as? BaseFragment)?.let { fragment ->
                fragments.remove(fragment)
                fragment.onBack()
                transaction.remove(fragment)
                OCMLogger.write("popFragment: ${fragment::class.java.simpleName}")
            }
            if (fragments.size < 2) {
                break
            }
        }
        transaction.show(fragments[fragments.size-1]).commitAllowingStateLoss()
//        layoutBack.visibility = if(fragments.size > 1) View.VISIBLE else View.INVISIBLE
    }

    protected fun replaceFragment(fragment: Fragment, animateType: FragmentAnimateType) {
        fragments.removeAll { true }
        fragments.add(fragment)
        OCMLogger.write("replaceFragment: ${fragment::class.java.simpleName}")
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
    fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !== PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2)
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 4)
            return
        }
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