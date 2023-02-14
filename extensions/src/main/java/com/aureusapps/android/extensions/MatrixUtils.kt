package com.aureusapps.android.extensions

import android.graphics.Matrix
import kotlin.math.atan2

/**
 * This class is not thread safe.
 */
object MatrixUtils {

    private val tempValues = FloatArray(9)
    private val tempPoint = FloatArray(2)

    /**
     * Returns translation or location of the origin point of transformed space relative to original space.
     *
     * @param matrix matrix to get translation.
     * @return translation x and y values.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * C and F gives the translation values tx and ty.
     */
    fun getTranslation(matrix: Matrix): Pair<Float, Float> {
        matrix.getValues(tempValues)
        return tempValues[2] to tempValues[5]
    }

    /**
     * Set translation or location of the origin point of transformed space relative to original space.
     *
     * @param matrix matrix to set translation.
     * @param tx translation x coordinate.
     * @param ty translation y coordinate.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * C and F gives the translation values tx and ty.
     */
    fun setTranslation(matrix: Matrix, tx: Float, ty: Float) {
        matrix.getValues(tempValues)
        tempValues[2] = tx
        tempValues[5] = ty
        matrix.setValues(tempValues)
    }

    /**
     * Returns the translation of the given point.
     */
    fun getTranslation(matrix: Matrix, px: Float, py: Float): Pair<Float, Float> {
        // find location of (px, py) on the transformed space.
        tempPoint[0] = px
        tempPoint[1] = py
        matrix.mapPoints(tempPoint)
        // find displacement of (px, py) on transformed space relative to (px, py)
        return (tempPoint[0] - px) to (tempPoint[1] - py)
    }

    /**
     * Set translation of the given point relative to the point on original space.
     */
    fun setTranslation(matrix: Matrix, tx: Float, ty: Float, px: Float, py: Float) {
        // get location of (px, py) on transformed space.
        tempPoint[0] = px
        tempPoint[1] = py
        matrix.mapPoints(tempPoint)
        // find difference in translation and add it to the translation values.
        matrix.getValues(tempValues)
        tempValues[2] += tx - tempPoint[0] + px
        tempValues[5] += ty - tempPoint[1] + py
        matrix.setValues(tempValues)
    }

    fun getRotation(matrix: Matrix): Float {
        matrix.getValues(tempValues)
        return atan2(tempValues[3], tempValues[4]).toDegrees()
    }

    fun setRotation(matrix: Matrix, degrees: Float) {

    }

    fun getRotation(matrix: Matrix, px: Float, py: Float): Float {
        return 0f
    }

    fun setRotation(matrix: Matrix, degrees: Float, px: Float, py: Float) {

    }

    fun getScaling(matrix: Float): Pair<Float, Float> {
        return 0f to 0f
    }

    fun setScaling(matrix: Matrix, sx: Float, sy: Float) {

    }

    fun getScaling(matrix: Matrix, px: Float, py: Float): Pair<Float, Float> {
        return 0f to 0f
    }

    fun setScaling(matrix: Matrix, sx: Float, sy: Float, px: Float, py: Float): Float {
        return 0f
    }

    fun decomposeComponents(matrix: Matrix): MatrixComponents {
        return MatrixComponents(0f, 0f to 0f, 0f to 0f, 0f to 0f)
    }

    fun decomposeComponents(matrix: Matrix, pivot: Pair<Float, Float>): MatrixComponents {
        return MatrixComponents(0f, 0f to 0f, 0f to 0f, 0f to 0f)
    }

    fun combineComponents(matrix: Matrix, components: MatrixComponents) {

    }

}