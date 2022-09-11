package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.res.Resources.Theme
import android.util.TypedValue
import androidx.annotation.AttrRes
import kotlin.math.roundToInt

fun Theme.resolveStyleAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Theme.hasAttribute(@AttrRes attr: Int): Boolean {
    return resolveAttribute(attr, TypedValue(), true)
}

fun Theme.resolveDimensionAttribute(@AttrRes attr: Int, default: Float = 0f): Float {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics)
        } else {
            default
        }
    }
}

fun Theme.resolvePixelDimensionAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics).roundToInt()
        } else {
            default
        }
    }
}

fun Theme.resolveColorAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.data
        } else {
            default
        }
    }
}

fun Theme.resolveDrawableAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

@SuppressLint("ResourceType")
fun Theme.obtainAndroidThemeOverlayId(): Int {
    val a = obtainStyledAttributes(intArrayOf(android.R.attr.theme, com.google.android.material.R.attr.theme))
    val androidThemeId = a.getResourceId(0, 0)
    val appThemeId = a.getResourceId(1, 0)
    return if (androidThemeId != 0) androidThemeId else appThemeId
}