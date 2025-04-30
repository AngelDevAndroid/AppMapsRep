package com.example.appmaps.ui.utils_code

import android.content.Context
import android.widget.Toast

object ReuseCode {
    // Toast
    fun msgToast(cxt: Context, msg: String, duration: Boolean) {

        var auxDuration = Toast.LENGTH_SHORT
        if (duration) {
            auxDuration = Toast.LENGTH_LONG
        }
        Toast.makeText(cxt, msg, auxDuration).show()
    }

    var getTokenUtils: String? = null
}