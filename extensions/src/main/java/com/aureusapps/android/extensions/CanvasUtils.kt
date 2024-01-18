package com.aureusapps.android.extensions

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path


object CanvasUtils {

    private val pathPool = ObjectPool { Path() }
    private val paintPool = ObjectPool { Paint() }

    /**
     * Draws a double-rounded rectangle on the specified [Canvas].
     *
     * @param canvas The canvas on which to draw the rectangle.
     * @param left The x-coordinate of the left side of the rectangle.
     * @param top The y-coordinate of the top side of the rectangle.
     * @param right The x-coordinate of the right side of the rectangle.
     * @param bottom The y-coordinate of the bottom side of the rectangle.
     * @param outerRadius The outer radius of the rounded corners.
     * @param innerRadius The inner radius of the rounded corners.
     * @param paint The paint used to style the rectangle.
     */
    fun drawRoundRect(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        outerRadius: Float,
        innerRadius: Float,
        paint: Paint
    ) {
        val drawPath = pathPool.acquire()
        val drawPaint = paintPool.acquire()
        drawPaint.set(paint)
        drawPaint.style = Paint.Style.FILL
        drawPath.apply {
            // outer rect
            reset()
            moveTo(left + outerRadius, top)
            arcTo(
                /* left = */ left,
                /* top = */ top,
                /* right = */ left + 2 * outerRadius,
                /* bottom = */ top + 2 * outerRadius,
                /* startAngle = */ 90f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(left, bottom - outerRadius)
            arcTo(
                /* left = */ left,
                /* top = */ bottom - 2 * outerRadius,
                /* right = */ left + 2 * outerRadius,
                /* bottom = */ bottom,
                /* startAngle = */ 180f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(right - outerRadius, bottom)
            arcTo(
                /* left = */ right - 2 * outerRadius,
                /* top = */ bottom - 2 * outerRadius,
                /* right = */ right,
                /* bottom = */ bottom,
                /* startAngle = */ 270f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(right, top + outerRadius)
            arcTo(
                /* left = */ right - 2 * outerRadius,
                /* top = */ top,
                /* right = */ right,
                /* bottom = */ top + 2 * outerRadius,
                /* startAngle = */ 0f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(left + outerRadius, top)
            close()

            // inner rect
            val strokeWidth = paint.strokeWidth
            moveTo(left + strokeWidth + innerRadius, top + strokeWidth)
            arcTo(
                /* left = */ left + strokeWidth,
                /* top = */ top + strokeWidth,
                /* right = */ left + strokeWidth + 2 * innerRadius,
                /* bottom = */ top + strokeWidth + 2 * innerRadius,
                /* startAngle = */ 90f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(left + strokeWidth, bottom - strokeWidth - innerRadius)

            arcTo(
                /* left = */ left + strokeWidth,
                /* top = */ bottom - strokeWidth - 2 * innerRadius,
                /* right = */ left + strokeWidth + 2 * innerRadius,
                /* bottom = */ bottom - strokeWidth,
                /* startAngle = */ 180f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(right - strokeWidth - innerRadius, bottom - strokeWidth)
            arcTo(
                /* left = */ right - strokeWidth - 2 * innerRadius,
                /* top = */ bottom - strokeWidth - 2 * innerRadius,
                /* right = */ right - strokeWidth,
                /* bottom = */ bottom - strokeWidth,
                /* startAngle = */ 270f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(right - strokeWidth, top + strokeWidth + innerRadius)
            arcTo(
                /* left = */ right - strokeWidth - 2 * innerRadius,
                /* top = */ top + strokeWidth,
                /* right = */ right - strokeWidth,
                /* bottom = */ top + strokeWidth + 2 * innerRadius,
                /* startAngle = */ 0f,
                /* sweepAngle = */ -90f,
                /* forceMoveTo = */ false
            )
            lineTo(left + strokeWidth + innerRadius, top + strokeWidth)
            close()
        }
        canvas.drawPath(drawPath, drawPaint)
        pathPool.release(drawPath)
        paintPool.release(drawPaint)
    }

}