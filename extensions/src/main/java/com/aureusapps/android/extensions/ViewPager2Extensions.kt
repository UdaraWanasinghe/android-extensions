package com.aureusapps.android.extensions

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

var ViewPager2.edgeEffectFactory: RecyclerView.EdgeEffectFactory
    get() = recyclerView.edgeEffectFactory
    set(value) {
        recyclerView.edgeEffectFactory = value
    }

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView