package com.aureusapps.android.extensions.test

import android.graphics.Matrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixExtensionsInstrumentedTest {

    companion object {
        private const val PRECISION = 0.01f
        private const val TRANSLATION_PRECISION = 0.01f
    }

    @Test
    fun testGetTranslation00() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = MatrixUtils.getTranslation(m)
        Assert.assertEquals(10f, tx, TRANSLATION_PRECISION)
        Assert.assertEquals(20f, ty, TRANSLATION_PRECISION)
    }

    @Test
    fun testSetTranslationTxTy00() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        MatrixUtils.setTranslation(m, 10f, 20f)
        val (tx, ty) = MatrixUtils.getTranslation(m)
        Assert.assertEquals(10f, tx, TRANSLATION_PRECISION)
        Assert.assertEquals(20f, ty, TRANSLATION_PRECISION)
    }

    @Test
    fun testGetTranslationPxPy() {
        val px = 5f
        val py = 5f
        val m = Matrix()

        // only translation
        m.setTranslate(10f, 20f)
        val (tx1, ty1) = MatrixUtils.getTranslation(m, px, py)
        Assert.assertEquals(10f, tx1, TRANSLATION_PRECISION)
        Assert.assertEquals(20f, ty1, TRANSLATION_PRECISION)

        // scaling followed by translation
        m.setScale(2f, 3f, px, py)
        m.postTranslate(10f, 20f)
        val (tx2, ty2) = MatrixUtils.getTranslation(m, px, py)
        Assert.assertEquals(10f, tx2, TRANSLATION_PRECISION)
        Assert.assertEquals(20f, ty2, TRANSLATION_PRECISION)

        // scaling, rotation and then translation
        m.setScale(2f, 3f, px, py)
        m.postRotate(30f, px, py)
        m.postTranslate(10f, 30f)
        val (tx3, ty3) = MatrixUtils.getTranslation(m, px, py)
        Assert.assertEquals(10f, tx3, TRANSLATION_PRECISION)
        Assert.assertEquals(30f, ty3, TRANSLATION_PRECISION)
    }

    @Test
    fun testSetTranslationTxTyPxPy() {
        val px = 5f
        val py = 5f
        val m = Matrix()

        m.setScale(2f, 2f, px, py)
        m.postRotate(30f, px, py)
        m.postTranslate(10f, 20f)

        MatrixUtils.setTranslation(m, 20f, 50f, px, py)

        val (tx, ty) = MatrixUtils.getTranslation(m, px, py)

        Assert.assertEquals(20f, tx, PRECISION)
        Assert.assertEquals(50f, ty, PRECISION)
    }

    @Test
    fun testGetRotation00() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        Assert.assertEquals(30f, MatrixUtils.getRotation(m), 0.1f)
    }

    @Test
    fun testSetRotationR00() {
        val m = Matrix()
        m.postScale(2f, 2f)
        MatrixUtils.setRotation(m, 30f)
        m.postTranslate(10f, 20f)
        Assert.assertEquals(30f, MatrixUtils.getRotation(m), 0.1f)
    }

    @Test
    fun testGetRotationPxPy() {
        // only rotate
        val m = Matrix()
        m.postRotate(45f)
        Assert.assertEquals(45f, MatrixUtils.getRotation(m), 0.1f)
        // rotate around pivot
        m.reset()
        m.postRotate(45f, 10f, 10f)
        Assert.assertEquals(45f, MatrixUtils.getRotation(m, 10f, 10f), 0.1f)
        // rotate around pivot and scale
        m.reset()
        m.postRotate(45f, 10f, 10f)
        m.postScale(2f, 2f, 10f, 10f)
        Assert.assertEquals(45f, MatrixUtils.getRotation(m, 10f, 10f), 0.1f)
        // rotate around pivot, scale and translate
        m.reset()
        m.postRotate(45f, 10f, 10f)
        m.postScale(2f, 2f, 10f, 10f)
        m.postTranslate(10f, 10f)
        Assert.assertEquals(45f, MatrixUtils.getRotation(m, 20f, 20f), 0.1f)
    }

    @Test
    fun testSetRotationRPxPy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        MatrixUtils.setRotation(m, 30f, 5f, 5f)
        m.postTranslate(10f, 10f)
        Assert.assertEquals(30f, MatrixUtils.getRotation(m, 5f, 5f))
    }

    @Test
    fun testGetScaling00() {
        // only scale
        val m = Matrix()
        m.postScale(2f, 2f)
        var s = MatrixUtils.getScaling(m)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        s = MatrixUtils.getScaling(m, 10f, 10f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot and rotate
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        m.postRotate(45f, 10f, 10f)
        s = MatrixUtils.getScaling(m, 10f, 10f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot, rotate and translate
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        m.postRotate(45f, 10f, 10f)
        m.postTranslate(10f, 10f)
        s = MatrixUtils.getScaling(m, 20f, 20f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
    }

    @Test
    fun testSetScalingS00() {
        val m = Matrix()
        MatrixUtils.setScaling(m, 2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (sx, sy) = MatrixUtils.getScaling(m)
        Assert.assertEquals(2f, sx, 0.1f)
        Assert.assertEquals(2f, sy, 0.1f)
    }

    @Test
    fun testGetScalingPxPy() {
        val m = Matrix()
        m.postScale(3f, 2f, 10f, 12f)
        val (sx, sy) = MatrixUtils.getScaling(m, 10f, 12f)
        Assert.assertEquals(3f, sx, 0.1f)
        Assert.assertEquals(2f, sy, 0.1f)
    }

    @Test
    fun testSetScalingSxSyPxPy() {
        val m = Matrix()
        m.postScale(3f, 2f, 10f, 12f)
        MatrixUtils.setScaling(m, 4f, 1f, 10f, 12f)
        val (sx, sy) = MatrixUtils.getScaling(m, 10f, 12f)
        Assert.assertEquals(4f, sx, 0.1f)
        Assert.assertEquals(1f, sy, 0.1f)
    }

    @Test
    fun testDecomposeComponents00() {
        val m = Matrix()
        m.postScale(2f, 3f)
        m.postRotate(35f)
        m.postTranslate(20f, 25f)
        val c = MatrixUtils.decomposeComponents(m)
        val (sx, sy) = c.scaling
        val r = c.rotation
        val (tx, ty) = c.translation
        Assert.assertEquals(2f, sx, 0.1f)
        Assert.assertEquals(3f, sy, 0.1f)
        Assert.assertEquals(35f, r, 0.1f)
        Assert.assertEquals(20f, tx, 0.1f)
        Assert.assertEquals(25f, ty, 0.1f)
    }

    @Test
    fun testDecomposeComponentsPxPy() {
        val m = Matrix()
        val px = 4f
        val py = 8f
        m.postScale(2f, 3f, px, py)
        m.postRotate(35f, px, py)
        MatrixUtils.setTranslation(m, 20f, 25f, px, py)
        val c = MatrixUtils.decomposeComponents(m, px to py)
        val (sx, sy) = c.scaling
        val r = c.rotation
        val (tx, ty) = c.translation
        Assert.assertEquals(2f, sx, 0.1f)
        Assert.assertEquals(3f, sy, 0.1f)
        Assert.assertEquals(35f, r, 0.1f)
        Assert.assertEquals(20f, tx, 0.1f)
        Assert.assertEquals(25f, ty, 0.1f)
    }

    @Test
    fun testCombineComponents() {
        val m = Matrix()
        val px = 4f
        val py = 8f
        MatrixUtils.combineComponents(
            m,
            MatrixComponents(
                2f to 3f,
                35f,
                20f to 25f,
                px to py
            )
        )
        val c = MatrixUtils.decomposeComponents(m, px to py)
        val (sx, sy) = c.scaling
        val r = c.rotation
        val (tx, ty) = c.translation
        Assert.assertEquals(2f, sx, 0.1f)
        Assert.assertEquals(3f, sy, 0.1f)
        Assert.assertEquals(35f, r, 0.1f)
        Assert.assertEquals(20f, tx, 0.1f)
        Assert.assertEquals(25f, ty, 0.1f)
    }

}