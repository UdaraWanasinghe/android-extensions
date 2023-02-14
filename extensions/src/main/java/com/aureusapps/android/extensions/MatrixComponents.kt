package com.aureusapps.android.extensions

data class MatrixComponents(
    val rotation: Float,
    val translation: Pair<Float, Float>,
    val scaling: Pair<Float, Float>,
    val pivot: Pair<Float, Float>
)