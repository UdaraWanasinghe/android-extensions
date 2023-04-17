package com.aureusapps.android.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

var ViewPager2.edgeEffectFactory: RecyclerView.EdgeEffectFactory
    get() = (getChildAt(0) as RecyclerView).edgeEffectFactory
    set(value) {
        (getChildAt(0) as RecyclerView).edgeEffectFactory = value
    }