package com.aureusapps.android.extensions

import android.view.ViewGroup.MarginLayoutParams

var MarginLayoutParams.horizontalMargin
    get() = leftMargin + rightMargin
    set(value) {
        leftMargin = value
        rightMargin = value
    }

var MarginLayoutParams.verticalMargin
    get() = topMargin + bottomMargin
    set(value) {
        topMargin = value
        bottomMargin = value
    }