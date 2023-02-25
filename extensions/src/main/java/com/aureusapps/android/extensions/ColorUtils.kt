package com.aureusapps.android.extensions

import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.roundToInt

object ColorUtils {
    fun transparentToOpaque(color: Int): Int {
        val alpha = color.alpha / 255f
        val r = 255 - (alpha * (255 - color.red)).roundToInt()
        val g = 255 - (alpha * (255 - color.green)).roundToInt()
        val b = 255 - (alpha * (255 - color.blue)).roundToInt()
        return Color.rgb(r, g, b)
    }
}