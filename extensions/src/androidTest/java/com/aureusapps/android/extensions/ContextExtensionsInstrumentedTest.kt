package com.aureusapps.android.extensions

import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextExtensionsInstrumentedTest {

    @Test
    fun testWrapTheme() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.TestStyle)
        val typedValue = TypedValue()

        if (!context.theme.resolveAttribute(R.attr.paddingNormal, typedValue, false)) {
            Assert.fail("Theme should have paddingNormal attribute")
        }
        Assert.assertEquals(48f.dp, typedValue.getDimension(context.resources.displayMetrics))

        val wrappedContext = context.wrapTheme(R.attr.testSubStyleAttr)
        if (!wrappedContext.theme.resolveAttribute(R.attr.paddingNormal, typedValue, false)) {
            Assert.fail("Theme should have paddingNormal attribute")
        }
        Assert.assertEquals(32f.dp, typedValue.getDimension(wrappedContext.resources.displayMetrics))
    }

}