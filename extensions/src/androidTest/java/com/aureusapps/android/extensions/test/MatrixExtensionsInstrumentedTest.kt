package com.aureusapps.android.extensions.test

import android.graphics.Matrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.getRotation
import com.aureusapps.android.extensions.rotation
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixExtensionsInstrumentedTest {

    @Test
    fun testMatrixRotationAroundPivot() {
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
        m.postScale(2f, 2f)
        Assert.assertEquals(45f, m.getRotation(10f, 10f))
        // rotate around pivot, scale and translate
        m.reset()
        m.postRotate(45f, 10f, 10f)
        m.postScale(2f, 2f)
        m.postTranslate(10f, 10f)
        Assert.assertEquals(45f, m.getRotation(20f, 20f))
    }

}