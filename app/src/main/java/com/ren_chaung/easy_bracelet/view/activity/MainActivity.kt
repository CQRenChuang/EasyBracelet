package com.ren_chaung.easy_bracelet.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import com.ren_chaung.easy_bracelet.view.fragment.InitFragment
import com.ren_chaung.easy_bracelet.view.fragment.MainFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragmentContainer(layoutMainContainer.id)
        title = getString(R.string.app_name)
        replaceFragment(InitFragment(object : InitFragment.InitFragmentListener {
            override fun initSuccess() {
                replaceFragment(MainFragment(), FragmentAnimateType.FADE)
            }
        }), FragmentAnimateType.FADE)
    }


}