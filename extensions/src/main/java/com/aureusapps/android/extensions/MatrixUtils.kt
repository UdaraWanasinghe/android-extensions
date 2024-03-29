package com.aureusapps.android.extensions

import android.graphics.Matrix
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utils to manipulate matrices.
 */
object MatrixUtils {

    private val matrixPool = ObjectPool { Matrix() }
    private val matrixArrayPool = ObjectPool { FloatArray(9) }

    private inline fun <R> getRelativeMatrix(matrix: Matrix, px: Float, py: Float, action: (Matrix) -> R): R {
        val matrix1 = matrixPool.acquire()
        val matrix2 = matrixPool.acquire()
        val matrix3 = matrixPool.acquire()
        return try {
            matrix1.setTranslate(-px, -py)
            matrix2.setTranslate(px, py)
            matrix1.invert(matrix3)
            matrix3.postConcat(matrix)
            matrix2.invert(matrix1)
            matrix3.postConcat(matrix1)
            action(matrix3)
        } finally {
            matrixPool.release(matrix1)
            matrixPool.release(matrix2)
            matrixPool.release(matrix3)
        }
    }

    private inline fun setRelativeMatrix(matrix: Matrix, px: Float, py: Float, action: (Matrix) -> Unit) {
        val matrix1 = matrixPool.acquire()
        val matrix2 = matrixPool.acquire()
        val matrix3 = matrixPool.acquire()
        try {
            matrix1.setTranslate(-px, -py)
            matrix2.setTranslate(px, py)
            matrix1.invert(matrix3)
            matrix3.postConcat(matrix)
            matrix2.invert(matrix1)
            matrix3.postConcat(matrix1)
            action(matrix3)
            matrix.setTranslate(-px, -py)
            matrix.postConcat(matrix3)
            matrix.postConcat(matrix2)
        } finally {
            matrixPool.release(matrix1)
            matrixPool.release(matrix2)
            matrixPool.release(matrix3)
        }
    }

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
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            return matrixArray[2] to matrixArray[5]
        } finally {
            matrixArrayPool.release(matrixArray)
        }
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
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            matrixArray[2] = tx
            matrixArray[5] = ty
            matrix.setValues(matrixArray)
        } finally {
            matrixArrayPool.release(matrixArray)
        }
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
        return getRelativeMatrix(matrix, px, py) { relativeMatrix ->
            getTranslation(relativeMatrix)
        }
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
        setRelativeMatrix(matrix, px, py) { relativeMatrix ->
            setTranslation(relativeMatrix, tx, ty)
        }
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
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            return atan2(matrixArray[3], matrixArray[0]).toDegrees()
        } finally {
            matrixArrayPool.release(matrixArray)
        }
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
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            val sx = sqrt(matrixArray[0] * matrixArray[0] + matrixArray[3] * matrixArray[3])
            val sy = sqrt(matrixArray[1] * matrixArray[1] + matrixArray[4] * matrixArray[4])
            val rad = degrees.toRadians()
            matrixArray[0] = sx * cos(rad)
            matrixArray[1] = -sy * sin(rad)
            matrixArray[3] = sx * sin(rad)
            matrixArray[4] = sy * cos(rad)
            matrix.setValues(matrixArray)
        } finally {
            matrixArrayPool.release(matrixArray)
        }
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
        return getRelativeMatrix(matrix, px, py) { relativeMatrix ->
            getRotation(relativeMatrix)
        }
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
        setRelativeMatrix(matrix, px, py) { relativeMatrix ->
            setRotation(relativeMatrix, degrees)
        }
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
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            val sx = sqrt(matrixArray[0] * matrixArray[0] + matrixArray[3] * matrixArray[3])
            val sy = sqrt(matrixArray[1] * matrixArray[1] + matrixArray[4] * matrixArray[4])
            return sx to sy
        } finally {
            matrixArrayPool.release(matrixArray)
        }
    }

    /**
     * Set scaling of the matrix relative to (0, 0) point on the original space.
     *
     * @param matrix Matrix to set scaling to.
     * @param sx Scaling on the x direction.
     * @param sy Scaling on the y direction.
     */
    fun setScaling(matrix: Matrix, sx: Float, sy: Float) {
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            val cr = atan2(matrixArray[3], matrixArray[0])
            matrixArray[0] = sx * cos(cr)
            matrixArray[1] = -sy * sin(cr)
            matrixArray[3] = sx * sin(cr)
            matrixArray[4] = sy * cos(cr)
            matrix.setValues(matrixArray)
        } finally {
            matrixArrayPool.release(matrixArray)
        }
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
        return getRelativeMatrix(matrix, px, py) { relativeMatrix ->
            getScaling(relativeMatrix)
        }
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
        setRelativeMatrix(matrix, px, py) { relativeMatrix ->
            setScaling(relativeMatrix, sx, sy)
        }
    }

    fun decomposeComponents(matrix: Matrix): MatrixComponents {
        val matrixArray = matrixArrayPool.acquire()
        try {
            matrix.getValues(matrixArray)
            val sx = sqrt(matrixArray[0] * matrixArray[0] + matrixArray[3] * matrixArray[3])
            val sy = sqrt(matrixArray[1] * matrixArray[1] + matrixArray[4] * matrixArray[4])
            val r = atan2(matrixArray[3], matrixArray[0]).toDegrees()
            val tx = matrixArray[2]
            val ty = matrixArray[5]
            return MatrixComponents(sx to sy, r, tx to ty, 0f to 0f)
        } finally {
            matrixArrayPool.release(matrixArray)
        }
    }

    fun decomposeComponents(matrix: Matrix, pivot: Pair<Float, Float>): MatrixComponents {
        val (px, py) = pivot
        return getRelativeMatrix(matrix, px, py) { relativeMatrix ->
            decomposeComponents(relativeMatrix)
        }
    }

    fun combineComponents(matrix: Matrix, components: MatrixComponents) {
        matrix.reset()
        val (px, py) = components.pivot
        val (sx, sy) = components.scaling
        val r = components.rotation
        val (tx, ty) = components.translation
        matrix.postScale(sx, sy, px, py)
        matrix.postRotate(r, px, py)
        matrix.postTranslate(tx, ty)
    }

}