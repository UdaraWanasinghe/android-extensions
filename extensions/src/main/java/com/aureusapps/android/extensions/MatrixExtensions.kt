package com.aureusapps.android.extensions


import android.graphics.Matrix
import androidx.core.graphics.values
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

val Matrix.translation: Pair<Float, Float>
    get() {
        val values = values()
        return values[2] to values[5]
    }

val Matrix.scaling: Float
    get() {
        val values = values()
        val sx = sqrt(values[0] * values[0] + values[3] * values[3])
        val sy = sqrt(values[1] * values[1] + values[4] * values[4])
        return (sx + sy) / 2
    }

val Matrix.rotation: Float
    get() {
        val values = values()
        return atan2(values[3], values[4]) * 180f / PI.toFloat()
    }

fun Matrix.getTranslation(pivotPoint: FloatArray): Pair<Float, Float> {
    mapPoints(pivotPoint)
    return pivotPoint[0] to pivotPoint[1]
}

fun Matrix.setTranslation(newTranslation: Pair<Float, Float>, pivotPoint: FloatArray) {
    mapPoints(pivotPoint)
    val (newTransX, newTransY) = newTranslation
    val values = values()
    values[2] += newTransX - pivotPoint[0]
    values[5] += newTransY - pivotPoint[1]
    setValues(values)
}

fun Matrix.setScaling(newScaling: Float, pivotPoint: Pair<Float, Float>) {
    // get old translation
    val (px, py) = pivotPoint
    val pp = floatArrayOf(px, py)
    mapPoints(pp)
    val tx = pp[0]
    val ty = pp[1]
    // update params
    val rotation = rotation
    reset()
    preScale(newScaling, newScaling, px, py)
    preRotate(rotation, px, py)
    // fix translation
    pp[0] = px
    pp[1] = py
    mapPoints(pp)
    val values = values()
    values[2] += tx - pp[0]
    values[5] += ty - pp[1]
    setValues(values)
}

fun Matrix.setRotation(newRotation: Float, pivotPoint: Pair<Float, Float>) {
    val (px, py) = pivotPoint
    val pp = floatArrayOf(px, py)
    // get old location of the pivot point
    // this is the translation of the object relative to pivot point
    mapPoints(pp)
    val tx = pp[0]
    val ty = pp[1]
    // update both scaling and rotation as changing one will change the other
    // save old
    val scaling = scaling
    // reset and update
    reset()
    preScale(scaling, scaling, px, py)
    preRotate(newRotation, px, py)
    // fix translation
    // find location of the pivot point
    pp[0] = px
    pp[1] = py
    mapPoints(pp)
    // bring pivot point back to its initial location
    val values = values()
    values[2] += tx - pp[0]
    values[5] += ty - pp[1]
    setValues(values)
}