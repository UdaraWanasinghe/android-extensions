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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Hides the soft keyboard associated with this view.
 * This extension function is used to hide the soft keyboard when called on a specific View.
 */
fun View.hideKeyboard() {
    context.getInputMethodManager().hideSoftInputFromWindow(
        windowToken, 0
    )
}

/**
 * Shows the soft keyboard associated with this view.
 * This extension function requests focus on the view and displays the soft keyboard.
 */
fun View.showKeyboard() {
    requestFocus()
    context.getInputMethodManager().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Measures the minimum size of the current View.
 *
 * This function internally measures the View with an unspecified size constraint to determine
 * its minimum width and height, and returns the measured dimensions as a Pair<Int, Int>,
 * where the first value represents the minimum width and the second value represents the minimum height.
 *
 * @return A Pair<Int, Int> representing the minimum width and height of the View.
 */
fun View.minimumSize(): Pair<Int, Int> {
    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(measureSpec, measureSpec)
    return measuredWidth to measuredHeight
}

/**
 * Traverses view tree and retrieves a ViewModel of type [T].
 *
 * @param storeProducer A function that produces a custom [ViewModelStore] for the ViewModel.
 *                      If not provided, it will traverse the view tree to find nearest view model store owner.
 * @param extrasProducer A function that produces custom [CreationExtras] for the ViewModel.
 *                       If not provided, it will traverse the view tree to find nearest view model provider factory
 *                       to retrieve default extras.
 * @param factoryProducer A function that produces a custom [ViewModelProvider.Factory] for the ViewModel.
 *                        If not provided, it will traverse the view tree to find nearest view model provider factory
 *                        to retrieve default factory.
 * @return A lazy delegate to access the ViewModel of type [T] in the view tree.
 */
inline fun <reified T : ViewModel> View.viewModels(
    noinline storeProducer: (() -> ViewModelStore)? = null,
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> {
    val storePromise = storeProducer ?: {
        val storeOwner = findViewTreeViewModelStoreOwner()
            ?: throw IllegalArgumentException("ViewModelStoreOwner is not found in the view tree")
        storeOwner.viewModelStore
    }
    val providerFactoryProducer by lazy {
        findViewTreeViewModelProviderFactory()
            ?: throw IllegalArgumentException("View model provider factory not found in the view tree")
    }
    val factoryPromise = factoryProducer ?: {
        providerFactoryProducer.defaultViewModelProviderFactory
    }
    val extrasPromise = extrasProducer ?: {
        providerFactoryProducer.defaultViewModelCreationExtras
    }
    return ViewModelLazy(T::class, storePromise, factoryPromise, extrasPromise)
}

/**
 * Traverses up the view hierarchy until it finds a view that holds a tag
 * with key [androidx.lifecycle.viewmodel.R.id.view_tree_view_model_store_owner] that implements
 * [HasDefaultViewModelProviderFactory]
 *
 * @return The [HasDefaultViewModelProviderFactory] associated with the view's view tree, or null if not found.
 */
fun View.findViewTreeViewModelProviderFactory(): HasDefaultViewModelProviderFactory? {
    return generateSequence(this) { view ->
        view.parent as? View
    }.mapNotNull { view ->
        view.getTag(androidx.lifecycle.viewmodel.R.id.view_tree_view_model_store_owner) as? HasDefaultViewModelProviderFactory
    }.firstOrNull()
}

/**
 * Attaches the `ViewModelStoreOwner` from the given [view]'s view tree to the current View.
 *
 * @param view The View from which to extract the `ViewModelStoreOwner`.
 * @throws NullPointerException if the `ViewModelStoreOwner` cannot be found.
 */
fun View.attachViewModelStoreOwnerFrom(view: View) {
    val storeOwner = view.findViewTreeViewModelStoreOwner() ?: throw NullPointerException("Failed to get view model store owner")
    setTag(androidx.lifecycle.viewmodel.R.id.view_tree_view_model_store_owner, storeOwner)
}

/**
 * Attaches the `LifecycleOwner` from the given [view]'s view tree to the current View.
 *
 * @param view The View from which to extract the `LifecycleOwner`.
 * @throws NullPointerException if the `LifecycleOwner` cannot be found.
 */
fun View.attachLifecycleOwnerFrom(view: View) {
    val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: throw NullPointerException("Failed to get lifecycle owner")
    setTag(androidx.lifecycle.runtime.R.id.view_tree_lifecycle_owner, lifecycleOwner)
}

/**
 * Detaches lifecycle owner from the view.
 */
fun View.detachLifecycleOwner() {
    setTag(androidx.lifecycle.runtime.R.id.view_tree_lifecycle_owner, null)
}

inline fun <reified T : ViewModel> View.activityViewModels(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = context.viewModels(extrasProducer, factoryProducer)

val View.lifecycleScope: LifecycleCoroutineScope
    get() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
            ?: throw IllegalStateException("Lifecycle owner not found in the view tree")
        return lifecycleOwner.lifecycleScope
    }

val View.lifecycle: Lifecycle
    get() {
        val lifecycleOwner = findViewTreeLifecycleOwner()
            ?: throw IllegalStateException("Lifecycle owner not found in the view tree")
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

fun View.resolveStyleAttribute(
    @AttrRes attr: Int,
    @StyleRes default: Int = 0
): Int {
    return theme.resolveStyleAttribute(attr, default)
}

fun View.resolveDrawableAttribute(
    @AttrRes attr: Int,
    @DrawableRes default: Int = 0
): Int {
    return theme.resolveDrawableAttribute(attr, default)
}

fun View.resolveColorAttribute(
    @AttrRes attr: Int,
    default: Int = Color.BLACK
): Int {
    return theme.resolveColorAttribute(attr, default)
}

fun View.resolveDimensionAttribute(
    @AttrRes attr: Int,
    default: Float = 0f
): Float {
    return theme.resolveDimensionAttribute(attr, default)
}

fun View.resolvePixelDimensionAttribute(
    @AttrRes attr: Int,
    default: Int = 0
): Int {
    return theme.resolvePixelDimensionAttribute(attr, default)
}

fun View.resolveBooleanAttribute(
    @AttrRes attr: Int,
    default: Boolean = false
): Boolean {
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

fun View.resolveDrawable(
    @AttrRes attr: Int,
    @DrawableRes default: Int = 0
): Drawable {
    return context.resolveDrawable(attr, default)
}

fun View.resolveIntArray(
    @AttrRes attr: Int,
    @ArrayRes default: Int = 0
): IntArray {
    return context.resolveIntArray(attr, default)
}

fun View.obtainStyledAttributes(
    set: AttributeSet?,
    attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
): TypedArray {
    return context.theme.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
}

/**
 * Obtains a `TypedArray` containing the styled attribute values in this `View`'s theme.
 *
 * @param attrs An array of resource IDs representing the desired styled attributes.
 * @return A `TypedArray` containing the styled attribute values.
 */
fun View.obtainStyledAttributes(attrs: IntArray): TypedArray {
    return context.theme.obtainStyledAttributes(attrs)
}

/**
 * Recursively searches for a Fragment associated with this View or its parent Views.
 *
 * @return The Fragment associated with this View or its parent Views, or null if not found.
 */
fun View.findFragment(): Fragment? {
    val tag = getTag(androidx.fragment.R.id.fragment_container_view_tag)
    if (tag is Fragment) {
        return tag
    }
    val parent = parent
    if (parent is View) {
        return parent.findFragment()
    }
    return null
}

/**
 * Finds and returns the nearest [FragmentManager] associated with the view.
 *
 * This finds the fragment manager associated with the view or the activity fragment
 * manager if the view is not associated with a fragment.
 *
 * @return The [FragmentManager] associated with the view, or null if not found.
 *
 * @see [FragmentManager.findFragment]
 * @see [fragmentManager]
 */
fun View.findFragmentManager(): FragmentManager? {
    return findFragment()?.childFragmentManager ?: context.fragmentManager
}

/**
 * Finds a Fragment by its tag within the view tree or in the parent fragment managers.
 *
 * @param tag The tag assigned to the Fragment.
 *
 * @return The Fragment if found, null otherwise.
 */
fun View.findFragmentByTag(tag: String): Fragment? {
    val fragment = findFragment()
    if (fragment != null) {
        val foundFragment = fragment.findFragmentByTag(tag)
        if (foundFragment != null) {
            return foundFragment
        }
    }
    return context.fragmentManager?.findFragmentByTag(tag)
}