package com.aureusapps.android.extensions

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

val RecyclerView.orientation: Int
    get() = when (val layoutManager = layoutManager) {
        is LinearLayoutManager -> {
            layoutManager.orientation
        }
        is GridLayoutManager -> {
            layoutManager.orientation
        }
        is StaggeredGridLayoutManager -> {
            layoutManager.orientation
        }
        else -> RecyclerView.VERTICAL
    }