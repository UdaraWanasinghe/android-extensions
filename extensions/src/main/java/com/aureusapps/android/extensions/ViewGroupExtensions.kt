package com.aureusapps.android.extensions

import android.view.View
import android.view.ViewGroup

fun ViewGroup.generateLayoutParams(): ViewGroup.LayoutParams {
    val method = ViewGroup::class.java.getDeclaredMethod("generateDefaultLayoutParams")
    method.isAccessible = true
    return method.invoke(this) as ViewGroup.LayoutParams
}

fun ViewGroup.generateLayoutParams(width: Int, height: Int): ViewGroup.LayoutParams {
    val layoutParams = generateLayoutParams()
    layoutParams.width = width
    layoutParams.height = height
    return layoutParams
}

fun ViewGroup.generateLayoutParams(
    width: Int,
    height: Int,
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0
): ViewGroup.LayoutParams {
    var layoutParams = generateLayoutParams()
    layoutParams.width = width
    layoutParams.height = height
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
    } else {
        layoutParams = ViewGroup.MarginLayoutParams(layoutParams)
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)
    }
    return layoutParams
}

fun ViewGroup.generateLayoutParams(
    width: Int,
    height: Int,
    margins: Int
): ViewGroup.LayoutParams {
    return generateLayoutParams(
        width,
        height,
        margins,
        margins,
        margins,
        margins
    )
}

inline fun <T : ViewGroup> T.addView(block: (T) -> View): T {
    addView(block(this))
    return this
}

inline fun <T : View, V : ViewGroup> V.addViews(list: List<T>, block: (V, T) -> View): V {
    list.forEach {
        val child = block(this, it)
        addView(child)
    }
    return this
}

inline fun <T : ViewGroup> T.addView(predicate: Boolean, block: (T) -> View): T {
    if (predicate) {
        return addView(block)
    }
    return this
}