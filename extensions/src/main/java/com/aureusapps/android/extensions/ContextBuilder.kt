package com.aureusapps.android.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.R

class ContextBuilder(private val context: Context) {

    companion object {
        private val ANDROID_THEME_OVERLAY_ATTRS = intArrayOf(android.R.attr.theme, R.attr.theme)
    }

    private var attrs: AttributeSet? = null
    private var themedContext: ContextThemeWrapper? = null
    private val targetContext get() = themedContext ?: context

    private fun getOrCreateThemedContext(): Context {
        return themedContext ?: ContextThemeWrapper(
            context,
            context.resources.newTheme().apply {
                // apply theme from the base context
                setTo(context.theme)
            }
        ).also { themedContext = it }
    }

    fun withAttrs(attrs: AttributeSet?): ContextBuilder {
        this.attrs = attrs
        return this
    }

    /**
     * Apply [defStyleRes] if [attrRes] is not in the theme.
     */
    fun useDefStyle(
        @AttrRes attrRes: Int,
        @StyleRes defStyleRes: Int
    ): ContextBuilder {
        val outValue = TypedValue()
        if (defStyleRes != 0 && !targetContext.theme.resolveAttribute(attrRes, outValue, false)) {
            // attribute is not in the context
            getOrCreateThemedContext().theme.applyStyle(defStyleRes, true)
        }
        return this
    }

    fun applyStyle(
        @AttrRes attrRes: Int,
        @StyleRes defStyleRes: Int = 0,
        force: Boolean = false
    ): ContextBuilder {
        val outValue = TypedValue()
        if (targetContext.theme.resolveAttribute(attrRes, outValue, false)) {
            getOrCreateThemedContext().theme.applyStyle(outValue.data, force)
        } else if (defStyleRes != 0) {
            getOrCreateThemedContext().theme.applyStyle(defStyleRes, force)
        }
        return this
    }

    fun applyStyle(
        @StyleRes styleRes: Int,
        force: Boolean = false
    ): ContextBuilder {
        if (styleRes != 0) {
            getOrCreateThemedContext().theme.applyStyle(styleRes, force)
        }
        return this
    }

    fun build(): Context {
        return themedContext?.also { themedContext ->
            // We want values set in android:theme or app:theme to always override values supplied.
            attrs?.let { attrs ->
                val androidThemeOverlayId = obtainAndroidThemeOverlayId(themedContext, attrs)
                if (androidThemeOverlayId != 0) {
                    themedContext.theme.applyStyle(androidThemeOverlayId, true)
                }
            }
        } ?: context
    }

    @SuppressLint("ResourceType")
    private fun obtainAndroidThemeOverlayId(context: Context, attrs: AttributeSet): Int {
        val a = context.obtainStyledAttributes(attrs, ANDROID_THEME_OVERLAY_ATTRS)
        val androidThemeId = a.getResourceId(0, 0)
        val appThemeId = a.getResourceId(1, 0)
        a.recycle()
        return if (androidThemeId != 0) androidThemeId else appThemeId
    }

}