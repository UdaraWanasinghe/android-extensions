package com.aureusapps.android.extensions

import kotlin.math.PI

fun Float.toDegrees(): Float {
    return (this * 180f / PI).toFloat()
}

fun Float.toRadians(): Float {
    return (this * PI / 180f).toFloat()
}