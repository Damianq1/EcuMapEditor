package com.winols.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class Waveform2DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var xValues: FloatArray = floatArrayOf()
    private var yValues: FloatArray = floatArrayOf()
    private var selectedIndex: Int = -1

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00FF66.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF3333.toInt()
        style = Paint.Style.FILL
    }

    private val selectedPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFF33.toInt()
        style = Paint.Style.FILL
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF666666.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFCCCCCC.toInt()
        textSize = 30f
    }

    private val path = Path()
    private var onPointSelectedListener: ((Int, Float, Float) -> Unit)? = null

    fun setData(x: FloatArray, y: FloatArray) {
        if (x.size != y.size) return
        xValues = x
        yValues = y
        selectedIndex = -1
        invalidate()
    }

    fun setOnPointSelectedListener(listener: (Int, Float, Float) -> Unit) {
        onPointSelectedListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (xValues.isEmpty() || yValues.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 80f

        val plotWidth = width - padding * 2
        val plotHeight = height - padding * 2

        val minX = xValues.minOrNull() ?: 0f
        val maxX = xValues.maxOrNull() ?: 1f
        val minY = yValues.minOrNull() ?: 0f
        val maxY = yValues.maxOrNull() ?: 1f

        val rangeX = if (maxX - minX == 0f) 1f else maxX - minX
        val rangeY = if (maxY - minY == 0f) 1f else maxY - minY

        canvas.drawRect(padding, padding, width - padding, height - padding, axisPaint)

        for (i in 0..4) {
            val yPos = padding + (plotHeight / 4) * i
            canvas.drawLine(padding, yPos, width - padding, yPos, gridPaint)
            val valLabel = maxY - (rangeY / 4) * i
            canvas.drawText(String.format("%.1f", valLabel), 10f, yPos + 10f, textPaint)
        }

        path.reset()
        val points = Array(xValues.size) { i ->
            val px = padding + ((xValues[i] - minX) / rangeX) * plotWidth
            val py = height - padding - ((yValues[i] - minY) / rangeY) * plotHeight
            Pair(px, py)
        }

        if (points.isNotEmpty()) {
            path.moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                path.lineTo(points[i].first, points[i].second)
            }
            canvas.drawPath(path, linePaint)

            for (i in points.indices) {
                val p = points[i]
                val paintToUse = if (i == selectedIndex) selectedPointPaint else pointPaint
                val radius = if (i == selectedIndex) 12f else 6f
                canvas.drawCircle(p.first, p.second, radius, paintToUse)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && xValues.isNotEmpty()) {
            val touchX = event.x
            val touchY = event.y
            val width = width.toFloat()
            val height = height.toFloat()
            val padding = 80f
            val plotWidth = width - padding * 2
            val plotHeight = height - padding * 2

            val minX = xValues.minOrNull() ?: 0f
            val maxX = xValues.maxOrNull() ?: 1f
            val minY = yValues.minOrNull() ?: 0f
            val maxY = yValues.maxOrNull() ?: 1f

            val rangeX = if (maxX - minX == 0f) 1f else maxX - minX
            val rangeY = if (maxY - minY == 0f) 1f else maxY - minY

            var closestIndex = -1
            var minDistance = Float.MAX_VALUE

            for (i in xValues.indices) {
                val px = padding + ((xValues[i] - minX) / rangeX) * plotWidth
                val py = height - padding - ((yValues[i] - minY) / rangeY) * plotHeight
                val dist = hypot(px - touchX, py - touchY)
                if (dist < minDistance && dist < 50f) {
                    minDistance = dist
                    closestIndex = i
                }
            }

            if (closestIndex != -1) {
                selectedIndex = closestIndex
                invalidate()
                onPointSelectedListener?.invoke(selectedIndex, xValues[selectedIndex], yValues[selectedIndex])
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
