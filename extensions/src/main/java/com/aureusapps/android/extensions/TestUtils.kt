package com.aureusapps.android.extensions

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

object ExtraMatchers {

    /**
     * Creates a Matcher<Int> that matches integers within the specified range (inclusive).
     *
     * @param start The start of the range.
     * @param last The last of the range.
     * @return A Matcher<Int> object that matches integers within the specified range.
     */
    fun inRange(start: Int, last: Int): Matcher<Int> {
        return InRangeMatcher(start, last)
    }

}

private class InRangeMatcher(
    private val first: Int,
    private val last: Int
) : BaseMatcher<Int>() {

    override fun describeTo(description: Description) {
        description.appendText("value should be in range [$first, $last]")
    }

    override fun matches(item: Any?): Boolean {
        if (item is Int) {
            return item in first..last
        }
        return false
    }

    override fun describeMismatch(item: Any?, mismatchDescription: Description) {
        if (item !is Int) {
            mismatchDescription.appendText("value is not an integer")
            return
        }

        mismatchDescription.appendText("value is $item, which is not in range [$first, $last]")
    }

}