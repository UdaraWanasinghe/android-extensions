package com.aureusapps.android.extensions

import android.graphics.BlendMode
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Converts a BlendMode enum value to the corresponding PorterDuff.Mode.
 *
 * This function is available starting from Android API level 29 (Q).
 *
 * @return The equivalent PorterDuff.Mode for the given BlendMode.
 *
 * @throws IllegalArgumentException if the provided BlendMode is not supported.
 *
 * @see BlendMode
 * @see PorterDuff.Mode
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun BlendMode.toPorterDuffMode(): PorterDuff.Mode = when (this) {
    BlendMode.CLEAR -> PorterDuff.Mode.CLEAR
    BlendMode.SRC -> PorterDuff.Mode.SRC
    BlendMode.DST -> PorterDuff.Mode.DST
    BlendMode.SRC_OVER -> PorterDuff.Mode.SRC_OVER
    BlendMode.DST_OVER -> PorterDuff.Mode.DST_OVER
    BlendMode.SRC_IN -> PorterDuff.Mode.SRC_IN
    BlendMode.DST_IN -> PorterDuff.Mode.DST_IN
    BlendMode.SRC_OUT -> PorterDuff.Mode.SRC_OUT
    BlendMode.DST_OUT -> PorterDuff.Mode.DST_OUT
    BlendMode.SRC_ATOP -> PorterDuff.Mode.SRC_ATOP
    BlendMode.DST_ATOP -> PorterDuff.Mode.DST_ATOP
    BlendMode.XOR -> PorterDuff.Mode.XOR
    BlendMode.DARKEN -> PorterDuff.Mode.DARKEN
    BlendMode.LIGHTEN -> PorterDuff.Mode.LIGHTEN
    BlendMode.MODULATE -> PorterDuff.Mode.MULTIPLY
    BlendMode.SCREEN -> PorterDuff.Mode.SCREEN
    BlendMode.PLUS -> PorterDuff.Mode.ADD
    BlendMode.OVERLAY -> PorterDuff.Mode.OVERLAY
    else -> throw IllegalArgumentException("Invalid blend mode: $this")
}