package com.aureusapps.android.extensions

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import com.google.android.material.R

fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextThemeWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}

fun Context.getInputMethodManager(): InputMethodManager {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return getSystemService(InputMethodManager::class.java)
    }
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}

fun Context.wrapTheme(@AttrRes attr: Int, @StyleRes defStyleRes: Int = 0): Context {
    val themeValue = TypedValue()
    return if (theme.resolveAttribute(attr, themeValue, false)) {
        ContextThemeWrapper(this, themeValue.data)
    } else if (defStyleRes != 0) {
        ContextThemeWrapper(this, defStyleRes)
    } else {
        this
    }
}

fun Context.obtainMaterialThemeOverlayId(
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): Int {
    val a = obtainStyledAttributes(
        attrs,
        intArrayOf(R.attr.materialThemeOverlay),
        defStyleAttr,
        defStyleRes
    )
    val materialThemeOverlayId = a.getResourceId(0, 0)
    a.recycle()
    return materialThemeOverlayId
}

fun Context.useDefaultMaterialTheme(
    @StyleRes themeResId: Int,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): Context {
    val materialThemeOverlayId = obtainMaterialThemeOverlayId(attrs, defStyleAttr, defStyleRes)
    if (materialThemeOverlayId == 0) {
        return android.view.ContextThemeWrapper(this, themeResId)
    }
    return this
}