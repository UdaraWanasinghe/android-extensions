package com.aureusapps.android.extensions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.test.R
import com.aureusapps.android.extensions.utils.TestHelpers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoUtilsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun test_generateMD5() {
        val resUri = TestHelpers.getAndroidResourceUri(context, R.raw.sample_text)
        val expectedMD5 = "6029f28561014cd2fccef51253be6dbb"
        val expectedSHA1 = "e666e67f66f4038e0c1d2f7c3a2abeaf27b3123f"
        val actualMD5 = CryptoUtils.generateMD5(context, resUri)
        val actualSHA1 = CryptoUtils.generateSHA1(context, resUri)
        Assert.assertEquals(expectedMD5, actualMD5)
        Assert.assertEquals(expectedSHA1, actualSHA1)
    }

}