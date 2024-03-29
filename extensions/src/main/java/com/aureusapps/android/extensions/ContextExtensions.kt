package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.R
import kotlin.math.roundToInt

@Suppress("RecursivePropertyAccessor")
val Context.fragmentManager: FragmentManager?
    get() = when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextWrapper -> baseContext.fragmentManager
        else -> null
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
    attrs: AttributeSet, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0
): Int {
    val a = obtainStyledAttributes(
        attrs, intArrayOf(R.attr.materialThemeOverlay), defStyleAttr, defStyleRes
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

/**
 * Apply the given style to the current context.
 *
 * @param styleResId The style resource id to apply.
 */
fun Context.applyStyle(@StyleRes styleResId: Int): Context {
    return ContextThemeWrapper(this, styleResId)
}

fun Context.resolveDrawable(@AttrRes attr: Int, @DrawableRes default: Int = 0): Drawable {
    return theme.resolveDrawableAttribute(attr, default).let {
        ContextCompat.getDrawable(this, it)!!
    }
}

fun Context.resolveIntArray(@AttrRes attr: Int, @ArrayRes default: Int = 0): IntArray {
    return theme.resolveIntArrayAttribute(attr, default).let {
        resources.getIntArray(it)
    }
}

/**
 * Get the activity viewModels from the context.
 */
inline fun <reified T : ViewModel> Context.viewModels(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> {
    val activity = componentActivity
        ?: throw IllegalStateException("Context is not a component activity")
    return activity.viewModels(extrasProducer, factoryProducer)
}

@Suppress("RecursivePropertyAccessor")
val Context.activity: Activity?
    get() = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }

@Suppress("RecursivePropertyAccessor")
val Context.fragmentActivity: FragmentActivity?
    get() = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.fragmentActivity
        else -> null
    }

@Suppress("RecursivePropertyAccessor")
val Context.appCompatActivity: AppCompatActivity?
    get() = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.appCompatActivity
        else -> null
    }

@Suppress("RecursivePropertyAccessor")
val Context.componentActivity: ComponentActivity?
    get() = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.componentActivity
        else -> null
    }

fun Context.resolveStyleAttribute(@AttrRes attr: Int, @StyleRes default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Context.hasAttribute(@AttrRes attr: Int): Boolean {
    return theme.resolveAttribute(attr, TypedValue(), true)
}

fun Context.resolveDimensionAttribute(@AttrRes attr: Int, default: Float = 0f): Float {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics)
        } else {
            default
        }
    }
}

fun Context.resolvePixelDimensionAttribute(@AttrRes attr: Int, default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.getDimension(resources.displayMetrics).roundToInt()
        } else {
            default
        }
    }
}

fun Context.resolveColorAttribute(@AttrRes attr: Int, default: Int = Color.BLACK): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.data
        } else {
            default
        }
    }
}

fun Context.resolveDrawableAttribute(@AttrRes attr: Int, @DrawableRes default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Context.resolveIntArrayAttribute(@AttrRes attr: Int, @ArrayRes default: Int = 0): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Context.resolveResourceIdAttribute(@AttrRes attr: Int, default: Int): Int {
    return TypedValue().let { typedValue ->
        if (theme.resolveAttribute(attr, typedValue, true)) {
            typedValue.resourceId
        } else {
            default
        }
    }
}

fun Context.resolveBooleanAttribute(@AttrRes attr: Int, default: Boolean = false): Boolean {
    return theme.resolveBooleanAttribute(attr, default)
}

@SuppressLint("ResourceType")
fun Context.obtainAndroidThemeOverlayId(): Int {
    val a = obtainStyledAttributes(
        intArrayOf(
            android.R.attr.theme,
            R.attr.theme
        )
    )
    val androidThemeId = a.getResourceId(0, 0)
    val appThemeId = a.getResourceId(1, 0)
    a.recycle()
    return if (androidThemeId != 0) androidThemeId else appThemeId
}