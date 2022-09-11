package com.aureusapps.android.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class UrlExtensionsInstrumentedTest {

    @Test
    fun testUriExtensions_convertHttpToHttps() {
        val url1 = URL("http://www.google.com/sample?q=test")
        val expected1 = URL("https://www.google.com/sample?q=test")
        Assert.assertEquals(expected1, url1.toHttps())
        val url2 = URL("https://www.google.com/sample?q=test")
        val expected2 = URL("https://www.google.com/sample?q=test")
        Assert.assertEquals(expected2, url2.toHttps())
    }

}