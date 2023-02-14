package com.aureusapps.android.extensions

import android.graphics.Matrix
import androidx.core.graphics.values
import kotlin.math.*

/**
 * Get translation or location of the origin point of transformed space relative to original space.
 */
val Matrix.translation: Pair<Float, Float>
    get() {
        val values = values()
        return values[2] to values[5]
    }

/**
 * Set translation or location of the origin point of transformed space relative to original space.
 *
 * @param tx New x coordinate of the origin point.
 * @param ty New y coordinate of the origin point.
 */
fun Matrix.setTranslation(tx: Float, ty: Float) {
    val values = values()
    values[2] = tx
    values[5] = ty
    setValues(values)
}

/**
 * Get translation of the point.
 * Gives location of the point relative to the location of the point on original space.
 *
 * @param px x coordinate of the point on original space.
 * @param py y coordinate of the point on original space.
 */
fun Matrix.getTranslation(px: Float, py: Float): Pair<Float, Float> {
    val pts = floatArrayOf(px, py)
    mapPoints(pts)
    return (pts[0] - px) to (pts[1] - py)
}

/**
 * Set translation of the point relative to the location of it in the original space.
 * This function is not thread safe
 *
 * @param tx x translation of [px] on original space.
 * @param ty y translation of [py] on original space.
 * @param px x coordinate of the point in original space.
 * @param py y coordinate of the point in original space.
 */
fun Matrix.setTranslation(tx: Float, ty: Float, px: Float, py: Float) {
    // get location of (px, py) on transformed space.
    val pts = floatArrayOf(px, py)
    mapPoints(pts)
    // find difference in translation and add it to the translation values.
    val values = values()
    values[2] += tx - pts[0] + px
    values[5] += ty - pts[1] + py
    setValues(values)
}

/**
 * Returns scaling around the origin.
 */
val Matrix.scaling: Pair<Float, Float>
    get() {
        val values = values()
        val sx = sqrt(values[0] * values[0] + values[3] * values[3])
        val sy = sqrt(values[1] * values[1] + values[4] * values[4])
        return sx to sy
    }

fun Matrix.setScaling(sx: Float, sy: Float) {
    val values = values()
    val theta = atan2(values[3], values[4])
    values[0] = sx * cos(theta)
    values[1] = sx * sin(theta)
    values[3] = -sy * sin(theta)
    values[4] = sy * cos(theta)
    setValues(values)
}

/**
 * Returns rotation around the origin in degrees.
 */
val Matrix.rotation: Float
    get() {
        val values = values()
        return atan2(values[3], values[4]) * 180f / PI.toFloat()
    }

/**
 * Get scaling around the pivot point.
 *
 * @param px pivot x
 * @param py pivot y
 */
fun Matrix.getScaling(px: Float, py: Float): Pair<Float, Float> {
    postTranslate(-px, -py)
    val s = scaling
    postTranslate(px, py)
    return s
}

/**
 * Get rotation around the pivot point.
 *
 * @param px pivot x
 * @param py pivot y
 */
fun Matrix.getRotation(px: Float, py: Float): Float {
    postTranslate(-px, -py)
    val r = rotation
    postTranslate(px, py)
    return r
}

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