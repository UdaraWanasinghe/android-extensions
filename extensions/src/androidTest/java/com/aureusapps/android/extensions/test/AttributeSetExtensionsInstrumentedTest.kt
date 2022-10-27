package com.aureusapps.android.extensions.test

import android.content.Context
import android.graphics.Color
import android.util.Xml
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aureusapps.android.extensions.forEachTag
import com.aureusapps.android.extensions.getColorAttribute
import com.aureusapps.android.extensions.getDimensionAttribute
import com.aureusapps.android.extensions.wrapTheme
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttributeSetExtensionsInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>().let {
        it.createConfigurationContext(it.resources.configuration.apply { densityDpi = 160 })
    }

    @Test
    fun testAttributeSetExtensions() {
        val expectedColors = listOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE
        )
        val expectedDimens = listOf(
            1f,
            2f,
            3f
        )
        var currentIndex = 0
        val context = context.wrapTheme(0, R.style.TestTheme)
        context.resources.getXml(R.drawable.vector_drawable).use { xml ->
            xml.forEachTag { parser ->
                when (parser.name) {
                    "path" -> {
                        val attrs = Xml.asAttributeSet(parser)
                        val fillColor = attrs.getColorAttribute(context, "fillColor")
                        Assert.assertEquals(expectedColors[currentIndex], fillColor)
                        val strokeWidth = attrs.getDimensionAttribute(context, "strokeWidth")
                        Assert.assertEquals(expectedDimens[currentIndex], strokeWidth)
                        currentIndex++
                    }
                }
            }
        }
    }

}