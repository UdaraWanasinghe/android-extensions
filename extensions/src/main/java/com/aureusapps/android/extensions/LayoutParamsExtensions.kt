package com.aureusapps.android.extensions

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams

fun LayoutParams.setVerticalMargin(margin: Int) {
    if (this is ViewGroup.MarginLayoutParams) {
        topMargin = margin
        bottomMargin = margin
    }
}

fun LayoutParams.setHorizontalMargin(margin: Int) {
    if (this is ViewGroup.MarginLayoutParams) {
        leftMargin = margin
        rightMargin = margin
    }
}