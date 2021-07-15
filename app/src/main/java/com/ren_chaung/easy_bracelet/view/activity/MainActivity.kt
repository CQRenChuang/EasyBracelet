package com.ren_chaung.easy_bracelet.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.comocm.base.extension.showToast
import com.ocm.bracelet_machine_sdk.BraceletMachineListener
import com.ocm.bracelet_machine_sdk.BraceletMachineManager
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.utils.extension.setOnMultiClickListener
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import com.ren_chaung.easy_bracelet.view.dialog.SetupNumDialog
import com.ren_chaung.easy_bracelet.view.fragment.InitFragment
import com.ren_chaung.easy_bracelet.view.fragment.MainFragment
import com.ren_chaung.easy_bracelet.view.fragment.SettingFragment
import com.tencent.bugly.beta.Beta
import kotlinx.android.synthetic.main.activity_base_fragment.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseFragmentActivity() {
    private var isInitSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragmentContainer(layoutMainContainer.id)
        title = getString(R.string.app_name)
        replaceFragment(InitFragment(object : InitFragment.InitFragmentListener {
            override fun initSuccess() {
                isInitSuccess = true
                replaceFragment(MainFragment(), FragmentAnimateType.FADE)
            }
        }), FragmentAnimateType.FADE)

        tvVersion.setOnMultiClickListener(3) {
            if (!isInitSuccess || fragments.size > 1) {
                showToast("开始检查更新")
                Beta.checkUpgrade()
                return@setOnMultiClickListener
            }
            push(SettingFragment())
        }

        layoutNumber.setOnMultiClickListener(5) {
            SetupNumDialog.create(this).show()
        }

        tvNumber.text = BraceletMachineManager.currentBracelet.toString()
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener {
            override fun onCurrentNumChange(num: Int) {
                tvNumber.text = num.toString()
            }
        })
    }


}