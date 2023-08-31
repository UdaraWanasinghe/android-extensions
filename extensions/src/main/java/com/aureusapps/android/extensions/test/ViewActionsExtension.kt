package com.aureusapps.android.extensions.test

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.CoreMatchers.any
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.Matcher
import java.util.Locale
import java.util.concurrent.TimeoutException

object ViewActionsExtension {

    private const val EDGE_FUZZ_FACTOR = 0.083f

    /**
     * Scroll [RecyclerView] to the given position.
     */
    fun scrollToPosition(position: Int, animate: Boolean = true): ViewAction {
        return ScrollToPositionAction(position, animate)
    }

    /**
     * Wait for [RecyclerView] or the [ViewPager2] to become idle.
     */
    fun waitForScroll(): ViewAction {
        return WaitForScrollAction()
    }

    /**
     * Wait for any matching view to become visible on the screen.
     */
    fun waitForAppear(
        viewMatcher: Matcher<View>,
        timeout: Long = 2000
    ): ViewAction {
        return WaitForAppearAction(viewMatcher, timeout)
    }

    /**
     * Perform swipe on the view.
     */
    fun swipeUp(
        swiper: Swiper = Swipe.FAST,
        startCoordinatesProvider: CoordinatesProvider = GeneralLocation.translate(
            GeneralLocationExtension.VISIBLE_BOTTOM,
            0f,
            -EDGE_FUZZ_FACTOR
        ),
        endCoordinatesProvider: CoordinatesProvider = GeneralLocationExtension.VISIBLE_TOP,
        precisionDescriber: PrecisionDescriber = Press.FINGER,
        minDisplayPercentage: Int = 10
    ): ViewAction {
        return SwipeUpAction(
            swiper,
            startCoordinatesProvider,
            endCoordinatesProvider,
            precisionDescriber,
            minDisplayPercentage
        )
    }

    private class ScrollToPositionAction(
        private val position: Int,
        private val animate: Boolean
    ) : ViewAction {

        override fun getDescription(): String {
            return "Scroll to position"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(RecyclerView::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            if (animate) {
                recyclerView.smoothScrollToPosition(position)
                uiController.loopMainThreadUntilIdle()
            } else {
                recyclerView.scrollToPosition(position)
                uiController.loopMainThreadUntilIdle()
            }
        }

    }

    private class WaitForScrollAction : ViewAction {

        override fun getDescription(): String {
            return "Wait for scroll"
        }

        override fun getConstraints(): Matcher<View> {
            return anyOf(
                ViewMatchers.isAssignableFrom(RecyclerView::class.java),
                ViewMatchers.isAssignableFrom(ViewPager2::class.java)
            )
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = when (view) {
                is ViewPager2 -> {
                    view.getChildAt(0) as RecyclerView
                }

                is RecyclerView -> {
                    view
                }

                else -> {
                    throw PerformException.Builder()
                        .withCause(IllegalArgumentException())
                        .withActionDescription(description)
                        .withViewDescription(HumanReadables.describe(view))
                        .build()
                }
            }
            while (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                uiController.loopMainThreadForAtLeast(100)
            }
        }

    }

    private class WaitForAppearAction(
        private val viewMatcher: Matcher<View>,
        private val timeout: Long
    ) : ViewAction {

        private val screenRect: Rect
        private val tempRect = Rect()
        private val tempIntArray = IntArray(2)

        init {
            val displayMetrics = Resources.getSystem().displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            screenRect = Rect(0, 0, screenWidth, screenHeight)
        }

        override fun getDescription(): String {
            return "Wait for view to appear"
        }

        override fun getConstraints(): Matcher<View> {
            return any(View::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            val endTime = System.currentTimeMillis() + timeout
            do {
                for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                    if (checkVisible(child)) {
                        return
                    }
                }
                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)
            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(description)
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }

        private fun checkVisible(child: View): Boolean {
            if (!viewMatcher.matches(child)) return false
            val rect = tempRect
            val location = tempIntArray
            rect.setEmpty()
            return if (child.getGlobalVisibleRect(rect)) {
                val rootView = child.rootView
                if (rootView != null) {
                    rootView.getLocationOnScreen(location)
                    rect.offset(location[0], location[1])
                }
                screenRect.contains(rect)
            } else {
                false
            }
        }

    }

    private class SwipeUpAction(
        private val swiper: Swiper,
        private val startCoordinatesProvider: CoordinatesProvider,
        private val endCoordinatesProvider: CoordinatesProvider,
        private val precisionDescriber: PrecisionDescriber,
        private val minDisplayPercentage: Int
    ) : ViewAction {

        companion object {
            private const val MAX_TRIES = 3
        }

        override fun getDescription(): String {
            return "Swipe up"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isDisplayingAtLeast(minDisplayPercentage)
        }

        override fun perform(uiController: UiController, view: View) {
            val startCoordinates = startCoordinatesProvider.calculateCoordinates(view)
            val endCoordinates = endCoordinatesProvider.calculateCoordinates(view)
            val precision = precisionDescriber.describePrecision()

            var status = Swiper.Status.FAILURE
            var tries = 0

            while (tries < MAX_TRIES && status != Swiper.Status.SUCCESS) {
                status = try {
                    swiper.sendSwipe(uiController, startCoordinates, endCoordinates, precision)
                } catch (re: RuntimeException) {
                    throw PerformException.Builder()
                        .withActionDescription(this.description)
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(re)
                        .build()
                }
                val duration = ViewConfiguration.getPressedStateDuration()
                // ensures that all work enqueued to process the swipe has been run.
                if (duration > 0) {
                    uiController.loopMainThreadForAtLeast(duration.toLong())
                }
                tries++
            }

            if (status == Swiper.Status.FAILURE) {
                throw PerformException.Builder()
                    .withActionDescription(description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(
                        RuntimeException(
                            String.format(
                                Locale.ROOT,
                                "Couldn't swipe from: %s,%s to: %s,%s precision: %s, %s . Swiper: %s "
                                        + "start coordinate provider: %s precision describer: %s. Tried %s times",
                                startCoordinates[0],
                                startCoordinates[1],
                                endCoordinates[0],
                                endCoordinates[1],
                                precision[0],
                                precision[1],
                                swiper,
                                startCoordinatesProvider,
                                precisionDescriber,
                                MAX_TRIES
                            )
                        )
                    )
                    .build()
            }
        }

    }

}