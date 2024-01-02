package com.aureusapps.android.extensions

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@IntDef(
    UiModeManagerCompat.MODE_LIGHT,
    UiModeManagerCompat.MODE_DARK,
    UiModeManagerCompat.MODE_AUTO
)
annotation class UiMode

object UiModeManagerCompat {

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_AUTO = 2
    private const val UI_MODE_PREFERENCES = "UI_MODE_PREFERENCES"
    private const val KEY_APP_UI_MODE = "KEY_APP_UI_MODE"

    fun applyUiMode(context: Context) {
        val preferences = context.getSharedPreferences(
           UI_MODE_PREFERENCES,
            Context.MODE_PRIVATE
        )
        val mode = preferences.getInt(
            KEY_APP_UI_MODE,
            MODE_AUTO
        )
        setUiModeInternal(context, mode)
    }

    fun setUiMode(context: Context, @UiMode mode: Int) {
        context.getSharedPreferences(
            UI_MODE_PREFERENCES,
            Context.MODE_PRIVATE
        ).edit(true) {
            putInt(KEY_APP_UI_MODE, mode)
        }
        setUiModeInternal(context, mode)
    }

    private fun setUiModeInternal(context: Context, mode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val actualMode = when (mode) {
                MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(actualMode)
        } else {
            val actualMode = when (mode) {
                MODE_LIGHT -> UiModeManager.MODE_NIGHT_NO
                MODE_DARK -> UiModeManager.MODE_NIGHT_YES
                else -> UiModeManager.MODE_NIGHT_AUTO
            }
            val modeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            modeManager.setApplicationNightMode(actualMode)
        }
    }

}