package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.res.Resources.Theme
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import kotlin.math.roundToInt

/**
 * Retrieve the style resource id of an attribute in the theme.
 */
fun Theme.resolveStyleAttribute(@AttrRes attr: Int, @StyleRes default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

/**
 * Check if an attribute is defined in the theme.
 */
fun Theme.hasAttribute(@AttrRes attr: Int): Boolean {
    return resolveAttribute(attr, TypedValue(), true)
}

/**
 * Retrieve the float dimension value of an attribute in the theme.
 */
fun Theme.resolveDimensionAttribute(@AttrRes attr: Int, default: Float = 0f): Float {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics)
        } else {
            default
        }
    }
}

/**
 * Retrieve the integer dimension value of an attribute in the theme.
 */
fun Theme.resolvePixelDimensionAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics).roundToInt()
        } else {
            default
        }
    }
}

/**
 * Retrieve the color value of an attribute in the theme.
 */
fun Theme.resolveColorAttribute(@AttrRes attr: Int, default: Int = Color.BLACK): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.data
        } else {
            default
        }
    }
}

/**
 * Retrieve the drawable resource id of an attribute in the theme.
 */
fun Theme.resolveDrawableAttribute(@AttrRes attr: Int, @DrawableRes default: Int = 0): Int {
    return resolveResourceIdAttribute(attr, default)
}

fun Theme.resolveIntArrayAttribute(@AttrRes attr: Int, @ArrayRes default: Int = 0): Int {
    return resolveResourceIdAttribute(attr, default)
}

fun Theme.resolveResourceIdAttribute(@AttrRes attr: Int, default: Int): Int {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Theme.resolveBooleanAttribute(@AttrRes attr: Int, default: Boolean = false): Boolean {
    return TypedValue().let { typedValue ->
        if (resolveAttribute(attr, typedValue, true)) {
            typedValue.data != 0
        } else {
            default
        }
    }
}

@SuppressLint("ResourceType")
fun Theme.obtainAndroidThemeOverlayId(): Int {
    val a = obtainStyledAttributes(
        intArrayOf(
            android.R.attr.theme,
            com.google.android.material.R.attr.theme
        )
    )
    val androidThemeId = a.getResourceId(0, 0)
    val appThemeId = a.getResourceId(1, 0)
    a.recycle()
    return if (androidThemeId != 0) androidThemeId else appThemeId
}