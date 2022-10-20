package com.aureusapps.android.extensions

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.*

fun View.dismissKeyboard() {
    context.getInputMethodManager().hideSoftInputFromWindow(
        windowToken, 0
    )
}

fun View.showKeyboard() {
    requestFocus()
    context.getInputMethodManager().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.minimumSize(): Pair<Int, Int> {
    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(measureSpec, measureSpec)
    return measuredWidth to measuredHeight
}

inline fun <reified T : ViewModel> View.viewModels(): Lazy<T> {
    val storeOwner = ViewTreeViewModelStoreOwner.get(this) ?: throw IllegalStateException(
        "View $this does not has a ViewModelStoreOwner set"
    )
    return lazy { ViewModelProvider(storeOwner).get(T::class.java) }
}

inline fun <reified T : ViewModel> View.viewModels(factory: ViewModelProvider.Factory): Lazy<T> {
    val storeOwner = ViewTreeViewModelStoreOwner.get(this) ?: throw IllegalStateException(
        "View $this does not has a ViewModelStoreOwner set"
    )
    return lazy { ViewModelProvider(storeOwner, factory).get(T::class.java) }
}

val View.lifecycleScope: LifecycleCoroutineScope
    get() {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: throw IllegalStateException(
            "View $this does not has a LifecycleOwner set"
        )
        return lifecycleOwner.lifecycleScope
    }

val View.lifecycle: Lifecycle
    get() {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: throw IllegalStateException(
            "View $this does not has a LifecycleOwner set"
        )
        return lifecycleOwner.lifecycle
    }

fun View.setWidth(width: Int) {
    val params = layoutParams
    params.width = width
    layoutParams = params
}

fun View.setHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

val View.verticalMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.topMargin + lp.bottomMargin
        }
        return 0
    }

val View.horizontalMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.leftMargin + lp.rightMargin
        }
        return 0
    }

val View.leftMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.leftMargin
        }
        return 0
    }

val View.rightMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.rightMargin
        }
        return 0
    }

val View.topMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.topMargin
        }
        return 0
    }

val View.bottomMargin: Int
    get() {
        val lp = layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            return lp.bottomMargin
        }
        return 0
    }

val View.theme: Resources.Theme
    get() = context.theme

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

fun View.getDrawable(@DrawableRes resourceId: Int): Drawable {
    return ResourcesCompat.getDrawable(resources, resourceId, theme)!!
}

fun View.setBackgroundResourceAttribute(@AttrRes attr: Int) {
    val resId = context.theme.resolveDrawableAttribute(attr)
    setBackgroundResource(resId)
}

fun View.resolveStyleAttribute(@AttrRes attr: Int, @StyleRes default: Int = 0): Int {
    return theme.resolveStyleAttribute(attr, default)
}

fun View.resolveDrawableAttribute(@AttrRes attr: Int, @DrawableRes default: Int = 0): Int {
    return theme.resolveDrawableAttribute(attr, default)
}

fun View.resolveColorAttribute(@AttrRes attr: Int, default: Int = Color.BLACK): Int {
    return theme.resolveColorAttribute(attr, default)
}

fun View.resolveDimensionAttribute(@AttrRes attr: Int, default: Float = 0f): Float {
    return theme.resolveDimensionAttribute(attr, default)
}

fun View.resolvePixelDimensionAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return theme.resolvePixelDimensionAttribute(attr, default)
}

fun View.setVerticalMargin(margin: Int) {
    val lp = layoutParams
    if (lp is ViewGroup.MarginLayoutParams) {
        lp.topMargin = margin
        lp.bottomMargin = margin
        layoutParams = lp
    }
}

fun View.setHorizontalMargin(margin: Int) {
    val lp = layoutParams
    if (lp is ViewGroup.MarginLayoutParams) {
        lp.leftMargin = margin
        lp.rightMargin = margin
        layoutParams = lp
    }
}

fun View.setVerticalPadding(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}

fun View.setHorizontalPadding(padding: Int) {
    setPadding(padding, paddingTop, padding, paddingBottom)
}

fun View.resolveDrawable(@AttrRes attr: Int,@DrawableRes default: Int = 0): Drawable {
    return context.resolveDrawable(attr, default)
}

fun View.resolveIntArray(@AttrRes attr: Int, @ArrayRes default: Int = 0): IntArray {
    return context.resolveIntArray(attr, default)
}