package com.aureusapps.android.extensions

import android.os.Build
import android.widget.TextView
import androidx.annotation.StyleRes

@Suppress("DEPRECATION")
fun TextView.setTextStyle(@StyleRes resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(resId)
    } else {
        setTextAppearance(context, resId)
    }
}