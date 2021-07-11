package com.ren_chaung.easy_bracelet.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ren_chaung.easy_bracelet.R
import com.ren_chaung.easy_bracelet.view.activity.BaseFragmentActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.app_name)
        setFragmentContainer(layoutMainContainer.id)
    }
}