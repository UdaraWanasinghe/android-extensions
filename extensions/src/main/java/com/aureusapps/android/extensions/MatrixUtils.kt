package com.aureusapps.android.extensions

import android.graphics.Matrix
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utils to manipulate matrices.
 * This class is not thread safe.
 */
object MatrixUtils {

    private val tempValues = FloatArray(9)
    private val tempPoint = FloatArray(2)

    /**
     * Returns translation or location of the origin point of the transformed space relative to the original space.
     *
     * @param matrix Matrix to get translation.
     * @return Translation x and y values.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * C(2) = tx
     *
     * F(5) = ty
     *
     */
    fun getTranslation(matrix: Matrix): Pair<Float, Float> {
        matrix.getValues(tempValues)
        return tempValues[2] to tempValues[5]
    }

    /**
     * Set translation or location of the origin point of the transformed space relative to the original space.
     *
     * @param matrix Matrix to set translation.
     * @param tx Translation x coordinate.
     * @param ty Translation y coordinate.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * C(2) = tx
     *
     * F(5) = ty
     */
    fun setTranslation(matrix: Matrix, tx: Float, ty: Float) {
        matrix.getValues(tempValues)
        tempValues[2] = tx
        tempValues[5] = ty
        matrix.setValues(tempValues)
    }

    /**
     * Returns the translation of the given point relative to it's location on the original space.
     *
     * @param matrix Matrix to get translation from.
     * @param px X coordinate of the point on the original space.
     * @param py Y coordinate of the point on the original space.
     * @return Translation of the given point relative to it's location on the original space.
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
     * Set translation of the given point relative to it's location on the original space.
     *
     * @param matrix Matrix to set translation to.
     * @param tx Translation x value.
     * @param ty Translation y value.
     * @param px X coordinate of the point.
     * @param py Y coordinate of the point.
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

    /**
     * Returns rotation of transformed space relative to (0, 0) point on the original space.
     *
     * @param matrix Matrix to get rotation from.
     * @return Rotation of the transformed space.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * A(0) = sx ✕ cos(a)
     *
     * B(1) = -sy ✕ sin(a)
     *
     * D(3) = sx ✕ sin(a)
     *
     * E(4) = sy ✕ cos(a)
     */
    fun getRotation(matrix: Matrix): Float {
        matrix.getValues(tempValues)
        return atan2(tempValues[3], tempValues[0]).toDegrees()
    }

    /**
     * Set rotation of the transformed space relative to (0, 0) point on the original space.
     *
     * @param matrix Matrix to set rotation to.
     * @param degrees Rotation to set.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * A(0) = sx ✕ cos(a)
     *
     * B(1) = -sy ✕ sin(a)
     *
     * D(3) = sx ✕ sin(a)
     *
     * E(4) = sy ✕ cos(a)
     */
    fun setRotation(matrix: Matrix, degrees: Float) {
        matrix.getValues(tempValues)
        val sx = sqrt(tempValues[0] * tempValues[0] + tempValues[3] * tempValues[3])
        val sy = sqrt(tempValues[1] * tempValues[1] + tempValues[4] * tempValues[4])
        val rad = degrees.toRadians()
        tempValues[0] = sx * cos(rad)
        tempValues[1] = -sy * sin(rad)
        tempValues[3] = sx * sin(rad)
        tempValues[4] = sy * cos(rad)
        matrix.setValues(tempValues)
    }

    /**
     * Returns rotation relative to the point (px, py) on the original space.
     *
     * @param matrix Matrix to get rotation from.
     * @param px X coordinate of the point on the original space.
     * @param py Y coordinate of the point on the original space.
     * @return Rotation relative to the point (px, py) on the original space.
     */
    fun getRotation(matrix: Matrix, px: Float, py: Float): Float {
        matrix.getValues(tempValues)
        return atan2(tempValues[3] - py * tempValues[6], tempValues[0] - px * tempValues[6]).toDegrees()
    }

    /**
     * Set rotation relative to the point (px, py) on the original space.
     *
     * @param matrix Matrix to set rotation to.
     * @param degrees Rotation to set.
     * @param px X coordinate of the point on the original space.
     * @param py Y coordinate of the point on the original space.
     */
    fun setRotation(matrix: Matrix, degrees: Float, px: Float, py: Float) {
        matrix.getValues(tempValues)
        val cr = atan2(tempValues[3] - py * tempValues[6], tempValues[0] - px * tempValues[6]).toDegrees()
        matrix.postRotate(degrees - cr, px, py)
    }

    /**
     * Get scaling of the matrix relative to (0, 0) point on the original space.
     *
     * @param matrix Matrix to get scaling from.
     * @return Scaling on the x and y directions.
     *
     * | A B C |
     *
     * | D E F |
     *
     * | G H I |
     *
     * sx = sqrt(A^2+D^2)
     *
     * sy = sqrt(B^2+E^2)
     */
    fun getScaling(matrix: Matrix): Pair<Float, Float> {
        matrix.getValues(tempValues)
        val sx = sqrt(tempValues[0] * tempValues[0] + tempValues[3] * tempValues[3])
        val sy = sqrt(tempValues[1] * tempValues[1] + tempValues[4] * tempValues[4])
        return sx to sy
    }

    /**
     * Set scaling of the matrix relative to (0, 0) point on the original space.
     *
     * @param matrix Matrix to set scaling to.
     * @param sx Scaling on the x direction.
     * @param sy Scaling on the y direction.
     */
    fun setScaling(matrix: Matrix, sx: Float, sy: Float) {
        matrix.getValues(tempValues)
        val cr = atan2(tempValues[3], tempValues[0]).toDegrees()
        tempValues[0] = sx * cos(cr)
        tempValues[1] = -sy * sin(cr)
        tempValues[3] = sx * sin(cr)
        tempValues[4] = sy * cos(cr)
        matrix.setValues(tempValues)
    }

    /**
     * Returns scaling of the transformed space relative to point (px, py) on the original space.
     *
     * @param matrix Matrix to get scaling from.
     * @param px X coordinate of the point on the original space.
     * @param py Y coordinate of the point on the original space.
     * @return Scaling relative to point (px, py) on the original space.
     */
    fun getScaling(matrix: Matrix, px: Float, py: Float): Pair<Float, Float> {
        matrix.getValues(tempValues)
        val a = tempValues[0] - px * tempValues[6]
        val b = tempValues[1] - px * tempValues[7]
        val d = tempValues[3] - py * tempValues[6]
        val e = tempValues[4] - py * tempValues[7]
        val sx = sqrt(a * a + d * d)
        val sy = sqrt(b * b + e * e)
        return sx to sy
    }

    /**
     * Set scaling of transformed space relative to the given point on the original space.
     *
     * @param matrix Matrix to set scaling to.
     * @param sx Scaling value in x direction.
     * @param sy Scaling value in y direction.
     * @param px X coordinate of the point.
     * @param py Y coordinate of the point.
     */
    fun setScaling(matrix: Matrix, sx: Float, sy: Float, px: Float, py: Float) {
        matrix.getValues(tempValues)
        val a = tempValues[0] - px * tempValues[6]
        val b = tempValues[1] - px * tempValues[7]
        val d = tempValues[3] - py * tempValues[6]
        val e = tempValues[4] - py * tempValues[7]
        val csx = sqrt(a * a + d * d)
        val csy = sqrt(b * b + e * e)
        matrix.postScale(sx / csx, sy / csy, px, py)
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