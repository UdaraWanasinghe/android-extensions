package com.aureusapps.android.extensions

import android.graphics.Matrix
import androidx.core.graphics.values
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Returns the translation.
 */
val Matrix.translation: Pair<Float, Float>
    get() {
        val values = values()
        return values[2] to values[5]
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
 * Set scaling around the pivot point.
 *
 * @param sx scaling x
 * @param sy scaling y
 * @param px pivot x
 * @param py pivot y
 */
fun Matrix.setScaling(sx: Float, sy: Float, px: Float, py: Float) {
    postTranslate(-px, -py)
    val cs = scaling
    val dsx = sx / cs.first
    val dsy = sy / cs.second
    postScale(dsx, dsy)
    postTranslate(px, py)
}

/**
 * Set rotation around the pivot point.
 *
 * @param r rotation around the pivot point
 * @param px pivot x
 * @param py pivot y
 */
fun Matrix.setRotation(r: Float, px: Float, py: Float) {
    postTranslate(-px, -py)
    val dr = r - rotation
    postRotate(dr)
    postTranslate(px, py)
}