package com.comocm.base.extension

import android.content.Context
import android.content.Intent
import android.widget.Toast

fun Context.showToast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Context.startNewActivity(cls: Class<*>) {
    val intent = Intent(this, cls)
    startActivity(intent)
}