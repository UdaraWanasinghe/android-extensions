package com.aureusapps.android.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.URL

@RunWith(AndroidJUnit4::class)
class UrlExtensionsInstrumentedTest {

    @Test
    fun testConvertHttpToHttps() {
        val url1 = URL("http://www.google.com/sample?q=test")
        val expected1 = URL("https://www.google.com/sample?q=test")
        Assert.assertEquals(expected1, url1.toHttps())
        val url2 = URL("https://www.google.com/sample?q=test")
        val expected2 = URL("https://www.google.com/sample?q=test")
        Assert.assertEquals(expected2, url2.toHttps())
    }

    @Test
    fun testDownloadFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val permissionCheckResult =
            pm.checkPermission(Manifest.permission.INTERNET, context.packageName)
        Assert.assertEquals(
            "Internet permission is not granted.",
            PackageManager.PERMISSION_GRANTED,
            permissionCheckResult
        )

        val srcUrl = URL("https://api.worldbank.org/v2/country?format=json")
        val dstFile = File(context.cacheDir, "test.json")
        if (dstFile.exists()) {
            dstFile.delete()
        }
        val dstUri = Uri.fromFile(dstFile)
        runBlocking {
            srcUrl.downloadFile(context, dstUri)
        }
        Assert.assertTrue(dstFile.exists())
    }

}