package com.aureusapps.android.extensions

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras

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

inline fun <reified T : ViewModel> View.viewModels(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = lazy {
    val storeOwner = findViewTreeViewModelStoreOwner() ?: throw IllegalStateException(
        "View $this does not has a ViewModelStoreOwner set"
    )
    val factory = factoryProducer?.invoke()
    val extras = extrasProducer?.invoke()
    return@lazy when {
        factory != null && extras != null -> {
            ViewModelProvider(storeOwner.viewModelStore, factory, extras)[T::class.java]
        }

        factory != null -> {
            ViewModelProvider(storeOwner, factory)[T::class.java]
        }

        else -> {
            ViewModelProvider(storeOwner)[T::class.java]
        }
    }
}

/**
 * Attaches the `ViewModelStoreOwner` from the given [view]'s view tree to the current View.
 *
 * @param view The View from which to extract the `ViewModelStoreOwner`.
 * @throws NullPointerException if the `ViewModelStoreOwner` cannot be found.
 */
private fun View.attachViewModelStoreOwnerFrom(view: View) {
    val storeOwner = view.findViewTreeViewModelStoreOwner()
        ?: throw NullPointerException("Failed to get view model store owner")
    setTag(androidx.lifecycle.viewmodel.R.id.view_tree_view_model_store_owner, storeOwner)
}

/**
 * Attaches the `LifecycleOwner` from the given [view]'s view tree to the current View.
 *
 * @param view The View from which to extract the `LifecycleOwner`.
 * @throws NullPointerException if the `LifecycleOwner` cannot be found.
 */
private fun View.attachLifecycleOwnerFrom(view: View) {
    val lifecycleOwner = view.findViewTreeLifecycleOwner()
        ?: throw NullPointerException("Failed to get lifecycle owner")
    setTag(androidx.lifecycle.runtime.R.id.view_tree_lifecycle_owner, lifecycleOwner)
}

inline fun <reified T : ViewModel> View.activityViewModels(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = context.viewModels(extrasProducer, factoryProducer)

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

val View.leftMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.leftMargin
        }
        return 0
    }

val View.rightMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.rightMargin
        }
        return 0
    }

val View.topMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.topMargin
        }
        return 0
    }

val View.bottomMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
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

fun View.resolveBooleanAttribute(@AttrRes attr: Int, default: Boolean = false): Boolean {
    return theme.resolveBooleanAttribute(attr, default)
}

var View.horizontalMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.leftMargin + lp.rightMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.leftMargin = value
                lp.rightMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.leftMargin = value
                newLp.rightMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.leftMargin = value
                newLp.rightMargin = value
                layoutParams = newLp
            }
        }
    }

var View.verticalMargin: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.topMargin + lp.bottomMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.topMargin = value
                lp.bottomMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.topMargin = value
                newLp.bottomMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.topMargin = value
                newLp.bottomMargin = value
                layoutParams = newLp
            }
        }
    }

var View.marginLeft: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.leftMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.leftMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.leftMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.leftMargin = value
                layoutParams = newLp
            }
        }
    }

var View.marginRight: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.rightMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.rightMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.rightMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.rightMargin = value
                layoutParams = newLp
            }
        }
    }

var View.marginTop: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.topMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.topMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.topMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.topMargin = value
                layoutParams = newLp
            }
        }
    }

var View.marginBottom: Int
    get() {
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            return lp.bottomMargin
        }
        return 0
    }
    set(value) {
        when (val lp = layoutParams) {
            is MarginLayoutParams -> {
                lp.bottomMargin = value
                layoutParams = lp
            }

            null -> {
                val newLp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                newLp.bottomMargin = value
                layoutParams = newLp
            }

            else -> {
                val newLp = MarginLayoutParams(lp)
                newLp.bottomMargin = value
                layoutParams = newLp
            }
        }
    }

var View.horizontalPadding
    get() = paddingLeft + paddingRight
    set(value) {
        setPadding(value, paddingTop, value, paddingBottom)
    }

var View.verticalPadding: Int
    get() = paddingTop + paddingBottom
    set(value) {
        setPadding(paddingLeft, value, paddingRight, value)
    }

var View.leftPadding
    get() = paddingLeft
    set(value) {
        setPadding(value, paddingTop, paddingRight, paddingBottom)
    }

var View.rightPadding
    get() = paddingRight
    set(value) {
        setPadding(paddingLeft, paddingTop, value, paddingBottom)
    }

var View.topPadding
    get() = paddingTop
    set(value) {
        setPadding(paddingLeft, value, paddingRight, paddingBottom)
    }

var View.bottomPadding
    get() = paddingBottom
    set(value) {
        setPadding(paddingLeft, paddingTop, paddingRight, value)
    }

fun View.resolveDrawable(@AttrRes attr: Int, @DrawableRes default: Int = 0): Drawable {
    return context.resolveDrawable(attr, default)
}

fun View.resolveIntArray(@AttrRes attr: Int, @ArrayRes default: Int = 0): IntArray {
    return context.resolveIntArray(attr, default)
}

fun View.obtainStyledAttributes(
    set: AttributeSet?, attrs: IntArray, defStyleAttr: Int = 0, defStyleRes: Int = 0
): TypedArray {
    return context.theme.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
}