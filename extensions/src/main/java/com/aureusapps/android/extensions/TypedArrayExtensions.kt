package com.aureusapps.android.extensions

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.StyleableRes

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
    getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }

fun TypedArray.getFloatOrFraction(index: Int, default: Float = 1f): Float {
    return when (getType(index)) {
        TypedValue.TYPE_FRACTION -> {
            getFraction(index, 1, 1, default)
        }
        TypedValue.TYPE_FLOAT -> {
            getFloat(index, default)
        }
        else -> {
            default
        }
    }
}

fun TypedArray.getIntArray(resources: Resources, @StyleableRes index: Int, @ArrayRes default: Int): IntArray {
    return resources.getIntArray(getResourceId(index, default))
}