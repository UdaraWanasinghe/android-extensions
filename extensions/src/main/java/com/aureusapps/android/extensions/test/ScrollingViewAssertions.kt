package com.aureusapps.android.extensions.test

import android.view.View
import androidx.core.view.ScrollingView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion

object ScrollingViewAssertions {

    /**
     * Check if the matching [ScrollingView] has scrolled vertically.
     */
    fun hasScrolledVertically(): ViewAssertion {
        return HasScrolledVerticallyAssertion()
    }

    /**
     * Check if the matching [ScrollingView] has not scrolled vertically.
     */
    fun hasNotScrolledVertically(): ViewAssertion {
        return HasNotScrolledVerticallyAssertion()
    }

    private class HasScrolledVerticallyAssertion : ViewAssertion {

        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            if (view !is ScrollingView) {
                throw AssertionError("View is not a ScrollingView")
            }
            val verticalScrollOffset = view.computeVerticalScrollOffset()
            if (verticalScrollOffset == 0) {
                throw AssertionError("The ScrollingView was expected to scroll vertically, but it did not scroll vertically.")
            }
        }

    }

    private class HasNotScrolledVerticallyAssertion : ViewAssertion {

        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            if (view !is ScrollingView) {
                throw AssertionError("View is not a ScrollingView")
            }
            val verticalScrollOffset = view.computeVerticalScrollOffset()
            if (verticalScrollOffset != 0) {
                throw AssertionError("The ScrollingView was expected to not scroll vertically, but it did scroll vertically.")
            }
        }

    }

}