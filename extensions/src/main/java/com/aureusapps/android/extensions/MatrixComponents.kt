package com.aureusapps.android.extensions

data class MatrixComponents(
    val scaling: Pair<Float, Float>,
    // rotation in degrees
    val rotation: Float,
    val translation: Pair<Float, Float>,
    val pivot: Pair<Float, Float>
)