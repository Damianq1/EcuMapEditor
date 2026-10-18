package com.winols.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Surface3DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var zValues: Array<FloatArray> = arrayOf()
    private var xLabels: FloatArray = floatArrayOf()
    private var yLabels: FloatArray = floatArrayOf()

    private var rotationX: Float = 30f
    private var rotationZ: Float = 45f
    private var scale: Float = 1.0f

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    private var selectedRow: Int = -1
    private var selectedCol: Int = -1

    private val meshPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3F51B5.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x333F51B5
        style = Paint.Style.FILL
    }

    private val selectedPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF5722.toInt()
        style = Paint.Style.FILL
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF666666.toInt()
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        textSize = 28f
    }

    private var onCellSelectedListener: ((Int, Int, Float) -> Unit)? = null

    fun setData(data: Array<FloatArray>, xVals: FloatArray = floatArrayOf(), yVals: FloatArray = floatArrayOf()) {
        this.zValues = data
        this.xLabels = xVals
        this.yLabels = yVals
        selectedRow = -1
        selectedCol = -1
        invalidate()
    }

    fun setOnCellSelectedListener(listener: (Int, Int, Float) -> Unit) {
        this.onCellSelectedListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                rotationZ += dx * 0.5f
                rotationX -= dy * 0.5f
                rotationX = rotationX.coerceIn(-89f, 89f)

                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (zValues.isEmpty() || zValues[0].isEmpty()) return

        val rows = zValues.size
        val cols = zValues[0].size

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height / 2f + 50f

        val minZ = zValues.minOf { it.minOrNull() ?: 0f }
        val maxZ = zValues.maxOf { it.maxOrNull() ?: 1f }
        val rangeZ = if (maxZ - minZ == 0f) 1f else maxZ - minZ

        val gridWidth = 400f * scale
        val gridHeight = 400f * scale

        val radX = Math.toRadians(rotationX.toDouble()).toFloat()
        val radZ = Math.toRadians(rotationZ.toDouble()).toFloat()

        val cosZ = cos(radZ)
        val sinZ = sin(radZ)
        val cosX = cos(radX)
        val sinX = sin(radX)

        fun project(rx: Float, ry: Float, rz: Float): Pair<Float, Float> {
            // Isometric / 3D rotation projection math
            val x1 = rx * cosZ - ry * sinZ
            val y1 = rx * sinZ + ry * cosZ
            val z1 = rz

            val x2 = x1
            val y2 = y1 * cosX - z1 * sinX
            // z2 unused for 2D screen projection directly

            return Pair(centerX + x2, centerY + y2)
        }

        val points = Array(rows) { r ->
            Array(cols) { c ->
                val nx = (c.toFloat() / (cols - 1).coerceAtLeast(1) - 0.5f) * gridWidth
                val ny = (r.toFloat() / (rows - 1).coerceAtLeast(1) - 0.5f) * gridHeight
                val nz = ((zValues[r][c] - minZ) / rangeZ - 0.5f) * 200f
                project(nx, ny, -nz)
            }
        }

        // Draw mesh lines
        for (r in 0 until rows) {
            val path = Path()
            for (c in 0 until cols) {
                val pt = points[r][c]
                if (c == 0) path.moveTo(pt.first, pt.second)
                else path.lineTo(pt.first, pt.second)
            }
            canvas.drawPath(path, meshPaint)
        }

        for (c in 0 until cols) {
            val path = Path()
            for (r in 0 until rows) {
                val pt = points[r][c]
                if (r == 0) path.moveTo(pt.first, pt.second)
                else path.lineTo(pt.first, pt.second)
            }
            canvas.drawPath(path, meshPaint)
        }

        // Draw selected point if any
        if (selectedRow in 0 until rows && selectedCol in 0 until cols) {
            val pt = points[selectedRow][selectedCol]
            canvas.drawCircle(pt.first, pt.second, 10f, selectedPointPaint)
        }
    }
}
