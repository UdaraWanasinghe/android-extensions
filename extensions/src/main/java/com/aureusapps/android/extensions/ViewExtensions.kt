package com.aureusapps.android.extensions

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun View.dismissKeyboard() {
    context.getInputMethodManager().hideSoftInputFromWindow(
        windowToken, 0
    )
}

fun View.showKeyboard() {
    context.getInputMethodManager().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.minimumSize(): Pair<Int, Int> {
    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(measureSpec, measureSpec)
    return measuredWidth to measuredHeight
}

fun View.setBackgroundResourceAttribute(@AttrRes attr: Int) {
    val resId = context.theme.resolveDrawableAttribute(attr)
    setBackgroundResource(resId)
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

fun View.animate(
    from: Int,
    to: Int,
    duration: Long,
    interpolator: Interpolator,
    callback: (Int) -> Unit
): Job {
    return lifecycleScope.launch {
        val animator = ValueAnimator.ofInt(from, to)
            .apply {
                this.duration = duration
                this.interpolator = interpolator
                this.addUpdateListener {
                    callback(animatedValue as Int)
                }
            }
        animator.start()
    }
}