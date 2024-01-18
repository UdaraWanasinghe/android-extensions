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
    fun drawDoubleRoundRect(
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
        val innerPath = pathPool.acquire()
        val drawPaint = paintPool.acquire()
        drawPaint.set(paint)
        drawPaint.style = Paint.Style.FILL
        val strokeWidth = paint.strokeWidth
        drawPath.reset()
        drawPath.addRoundRect(
            left,
            top,
            right,
            bottom,
            outerRadius,
            outerRadius,
            Path.Direction.CW
        )
        innerPath.reset()
        innerPath.addRoundRect(
            left + strokeWidth,
            top + strokeWidth,
            right - strokeWidth,
            bottom - strokeWidth,
            innerRadius,
            innerRadius,
            Path.Direction.CW
        )
        drawPath.op(innerPath, Path.Op.DIFFERENCE)
        canvas.drawPath(drawPath, drawPaint)
        pathPool.release(drawPath)
        pathPool.release(innerPath)
        paintPool.release(drawPaint)
    }

}