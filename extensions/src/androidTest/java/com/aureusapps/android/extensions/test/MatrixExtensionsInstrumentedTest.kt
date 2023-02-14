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
    fun testGetTranslation() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = m.translation
        Assert.assertEquals(tx, 10f, 0.1f)
        Assert.assertEquals(ty, 20f, 0.1f)
    }

    @Test
    fun testSetTranslationTxTy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.setTranslation(10f, 20f)
        val (tx, ty) = m.translation
        Assert.assertEquals(tx, 10f, 0.1f)
        Assert.assertEquals(ty, 20f, 0.1f)
    }

    @Test
    fun testGetTranslationPxPy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = m.getTranslation(5f, 5f)
        Assert.assertEquals(8.66f, tx, 0.1f)
        Assert.assertEquals(28.66f, ty, 0.1f)
    }

    @Test
    fun testSetTranslationTxTyPxPy() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        m.setTranslation(30f, 30f, 5f, 5f)
        val (tx, ty) = m.getTranslation(5f, 5f)
        Assert.assertEquals(30f, tx, 0.1f)
        Assert.assertEquals(30f, ty, 0.1f)
    }

    @Test
    fun testGetRotation() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val rotation = m.rotation
        Assert.assertEquals(30f, rotation, 0.1f)
    }

    @Test
    fun testSetRotation() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.setRotation(30f)
        m.postTranslate(10f, 20f)
        val rotation = m.rotation
        Assert.assertEquals(30f, rotation, 0.1f)
    }

    @Test
    fun testMatrixGetRotationAroundPivot() {
        // only rotate
        val m = Matrix()
        m.postRotate(45f)
        Assert.assertEquals(45f, m.rotation)
        // rotate around pivot
        m.reset()
        m.postRotate(45f, 10f, 10f)
        Assert.assertEquals(45f, m.getRotation(10f, 10f))
        // rotate around pivot and scale
        m.reset()
        m.postRotate(45f, 10f, 10f)
        m.postScale(2f, 2f, 10f, 10f)
        Assert.assertEquals(45f, m.getRotation(10f, 10f))
        // rotate around pivot, scale and translate
        m.reset()
        m.postRotate(45f, 10f, 10f)
        m.postScale(2f, 2f, 10f, 10f)
        m.postTranslate(10f, 10f)
        Assert.assertEquals(45f, m.getRotation(20f, 20f))
    }

    @Test
    fun testMatrixGetScalingAroundPivot() {
        // only scale
        val m = Matrix()
        m.postScale(2f, 2f)
        var s = m.scaling
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        s = m.getScaling(10f, 10f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot and rotate
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        m.postRotate(45f, 10f, 10f)
        s = m.getScaling(10f, 10f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
        // scale around pivot, rotate and translate
        m.reset()
        m.postScale(2f, 2f, 10f, 10f)
        m.postRotate(45f, 10f, 10f)
        m.postTranslate(10f, 10f)
        s = m.getScaling(20f, 20f)
        Assert.assertEquals(2f, s.first, 0.00001f)
        Assert.assertEquals(2f, s.second, 0.00001f)
    }

    @Test
    fun testSetTranslation() {
        val m = Matrix()
        m.postScale(2f, 2f)
        m.postRotate(30f)
        m.postTranslate(10f, 20f)
        val (tx, ty) = m.translation
        Assert.assertEquals(10f, tx, 0.1f)
        Assert.assertEquals(20f, ty, 0.1f)
    }

}