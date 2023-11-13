package com.aureusapps.android.extensions.test

import android.graphics.Rect
import android.view.View
import androidx.test.espresso.action.CoordinatesProvider

enum class GeneralLocationExtension : CoordinatesProvider {

    VISIBLE_BOTTOM {
        override fun calculateCoordinates(view: View): FloatArray {
            return getCoordinatesOfVisiblePart(view, Position.END, Position.MIDDLE)
        }
    },

    VISIBLE_TOP {
        override fun calculateCoordinates(view: View): FloatArray {
            return getCoordinatesOfVisiblePart(view, Position.BEGIN, Position.MIDDLE)
        }
    },

    VISIBLE_LEFT {
        override fun calculateCoordinates(view: View): FloatArray {
            return getCoordinatesOfVisiblePart(view, Position.MIDDLE, Position.BEGIN)
        }
    },

    VISIBLE_RIGHT {
        override fun calculateCoordinates(view: View): FloatArray {
            return getCoordinatesOfVisiblePart(view, Position.MIDDLE, Position.END)
        }
    };

    protected fun getCoordinatesOfVisiblePart(
        view: View,
        vertical: Position,
        horizontal: Position
    ): FloatArray {
        val visibleParts = Rect()
        return if (view.getGlobalVisibleRect(visibleParts)) {
            val xy = IntArray(2)
            view.getLocationOnScreen(xy)
            val rootView = view.rootView
            val rootLocation = IntArray(2)
            if (rootView != null) {
                rootView.getLocationOnScreen(rootLocation)
                visibleParts.offset(rootLocation[0], rootLocation[1])
            }
            val x = horizontal.getPosition(xy[0], visibleParts.width())
            val y = vertical.getPosition(xy[1], visibleParts.height())
            floatArrayOf(x, y)
        } else {
            throw IllegalStateException("View is not visible on the screen")
        }
    }

    protected enum class Position {

        BEGIN {
            override fun getPosition(widgetPos: Int, widgetLength: Int): Float {
                return widgetPos.toFloat()
            }
        },

        MIDDLE {
            override fun getPosition(widgetPos: Int, widgetLength: Int): Float {
                // Midpoint between the leftmost and rightmost pixel (position viewLength - 1).
                return widgetPos + (widgetLength - 1) / 2.0f
            }
        },

        END {
            override fun getPosition(widgetPos: Int, widgetLength: Int): Float {
                return (widgetPos + widgetLength - 1).toFloat()
            }
        };

        abstract fun getPosition(widgetPos: Int, widgetLength: Int): Float
    }

}