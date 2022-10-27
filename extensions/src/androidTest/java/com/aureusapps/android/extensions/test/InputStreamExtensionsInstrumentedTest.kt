package com.aureusapps.android.extensions.test

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.writeTo
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileReader

@RunWith(AndroidJUnit4::class)
class InputStreamExtensionsInstrumentedTest {

    @Test
    fun testWriteTo() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputStream = javaClass.classLoader?.getResourceAsStream("test.txt")
        val file = File(context.cacheDir, "test.txt")
        if (file.exists()) {
            file.delete()
        }
        val uri = Uri.fromFile(file)
        inputStream?.writeTo(uri, context)
        Assert.assertTrue(file.exists())
        val text = FileReader(file).readText()
        Assert.assertEquals("Hello World", text)
    }

}