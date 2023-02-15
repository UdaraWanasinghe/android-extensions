package com.aureusapps.android.extensions.test

import android.graphics.Matrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixExtensionsInstrumentedTest {

    @Test
    fun testGetTranslation00() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = MatrixUtils.getTranslation(m)
        Assert.assertEquals(10f, tx, 0.1f)
        Assert.assertEquals(20f, ty, 0.1f)
    }

    @Test
    fun testSetTranslationTxTy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        MatrixUtils.setTranslation(m, 10f, 20f)
        val (tx, ty) = MatrixUtils.getTranslation(m)
        Assert.assertEquals(10f, tx, 0.1f)
        Assert.assertEquals(20f, ty, 0.1f)
    }

    @Test
    fun testGetTranslationPxPy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = MatrixUtils.getTranslation(m, 5f, 5f)
        Assert.assertEquals(8.66f, tx, 0.1f)
        Assert.assertEquals(28.66f, ty, 0.1f)
    }

    @Test
    fun testSetTranslationTxTyPxPy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        MatrixUtils.setTranslation(m, 30f, 30f, 5f, 5f)
        val (tx, ty) = MatrixUtils.getTranslation(m, 5f, 5f)
        Assert.assertEquals(30f, tx, 0.1f)
        Assert.assertEquals(30f, ty, 0.1f)
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
        m.setRotation(30f)
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
    fun testGetScaling() {
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
    fun testSetScalingS() {
        val m = Matrix()
        m.setScaling(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (sx, sy) = m.scaling
        Assert.assertEquals(2f, sx, 0.1f)
        Assert.assertEquals(2f, sy, 0.1f)
    }

}