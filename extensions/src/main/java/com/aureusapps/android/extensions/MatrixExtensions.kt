package com.aureusapps.android.extensions

import android.graphics.Matrix

val Matrix.translation: Pair<Float, Float>
    get() = MatrixUtils.getTranslation(this)

fun Matrix.setTranslation(tx: Float, ty: Float) =
    MatrixUtils.setTranslation(this, tx, ty)

fun Matrix.getTranslation(px: Float, py: Float): Pair<Float, Float> =
    MatrixUtils.getTranslation(this, px, py)

fun Matrix.setTranslation(tx: Float, ty: Float, px: Float, py: Float) =
    MatrixUtils.setTranslation(this, tx, ty, px, py)

val Matrix.rotation: Float
    get() = MatrixUtils.getRotation(this)

fun Matrix.setRotation(degrees: Float) =
    MatrixUtils.setRotation(this, degrees)

fun Matrix.getRotation(px: Float, py: Float): Float =
    MatrixUtils.getRotation(this, px, py)

fun Matrix.setRotation(degrees: Float, px: Float, py: Float) =
    MatrixUtils.setRotation(this, degrees, px, py)

val Matrix.scaling: Pair<Float, Float>
    get() = MatrixUtils.getScaling(this)

fun Matrix.setScaling(sx: Float, sy: Float) =
    MatrixUtils.setScaling(this, sx, sy)

fun Matrix.getScaling(px: Float, py: Float): Pair<Float, Float> =
    MatrixUtils.getScaling(this, px, py)

fun Matrix.setScaling(sx: Float, sy: Float, px: Float, py: Float) =
    MatrixUtils.setScaling(this, sx, sy, px, py)

/**
 * Returns a copy of the matrix.
 *
 * @param matrix Optional matrix to copy to.
 * @return A copy of the matrix.
 */
fun Matrix.copy(matrix: Matrix = Matrix()): Matrix {
    matrix.set(this)
    return matrix
}

/**
 * Multiplies this matrix by the given matrix. M' = M * other
 */
operator fun Matrix.times(matrix: Matrix): Matrix {
    val m = Matrix(this)
    m.preConcat(matrix)
    return m
}