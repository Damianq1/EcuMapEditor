package com.winols.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HexGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: ByteArray = ByteArray(0)
    private var bytesPerRow: Int = 16
    private var cellWidth: Float = 120f
    private var cellHeight: Float = 60f
    private var headerWidth: Float = 140f
    private var headerHeight: Float = 60f

    private var selectionStart: Int = -1
    private var selectionEnd: Int = -1

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = 0xFF212121.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = 0xFF424242.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBDBDBD.toInt()
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt()
        style = Paint.Style.FILL
    }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x40009688.toInt()
        style = Paint.Style.FILL
    }

    private val cellBounds = Rect()

    private var onCellClickListener: ((Int) -> Unit)? = null
    private var onSelectionChangedListener: ((Int, Int) -> Unit)? = null

    fun setData(newData: ByteArray, bytesPerRow: Int = 16) {
        this.data = newData
        this.bytesPerRow = bytesPerRow
        requestLayout()
        invalidate()
    }

    fun getData(): ByteArray = data

    fun setOnCellClickListener(listener: (Int) -> Unit) {
        onCellClickListener = listener
    }

    fun setOnSelectionChangedListener(listener: (Int, Int) -> Unit) {
        onSelectionChangedListener = listener
    }

    fun applyConstant(value: Int) {
        if (selectionStart < 0 || selectionEnd < 0) return
        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        for (i in start..end) {
            if (i in data.indices) {
                data[i] = value.toByte()
            }
        }
        invalidate()
    }

    fun applyPercentage(percentage: Double) {
        if (selectionStart < 0 || selectionEnd < 0) return
        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        val factor = 1.0 + (percentage / 100.0)
        for (i in start..end) {
            if (i in data.indices) {
                val current = data[i].toInt() and 0xFF
                val newVal = (current * factor).toInt().coerceIn(0, 255)
                data[i] = newVal.toByte()
            }
        }
        invalidate()
    }

    fun applySmoothing() {
        if (selectionStart < 0 || selectionEnd < 0) return
        val start = min(selectionStart, selectionEnd)
        val end = max(selectionStart, selectionEnd)
        if (end - start < 2) return

        val copy = data.clone()
        for (i in (start + 1)..<end) {
            if (i in data.indices && i > 0) {
                val prev = copy[i - 1].toInt() and 0xFF
                val curr = copy[i].toInt() and 0xFF
                val next = copy[i + 1].toInt() and 0xFF
                val smoothed = ((prev + curr + next) / 3).coerceIn(0, 255)
                data[i] = smoothed.toByte()
            }
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val numRows = if (bytesPerRow > 0) (data.size + bytesPerRow - 1) / bytesPerRow else 0
        val totalWidth = (headerWidth + bytesPerRow * cellWidth).toInt()
        val totalHeight = (headerHeight + numRows * cellHeight).toInt()

        val resolvedWidth = resolveSize(totalWidth, widthMeasureSpec)
        val resolvedHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val numRows = (data.size + bytesPerRow - 1) / bytesPerRow

        // Draw top header background & column headers
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, headerBgPaint)
        for (c in 0 until bytesPerRow) {
            val left = headerWidth + c * cellWidth
            val text = String.format("%02X", c)
            canvas.drawText(text, left + cellWidth / 2f, headerHeight * 0.65f, headerTextPaint)
            canvas.drawLine(left, 0f, left, headerHeight, gridPaint)
        }
        canvas.drawLine(0f, headerHeight, headerWidth + bytesPerRow * cellWidth, headerHeight, gridPaint)

        // Draw rows
        val minSel = if (selectionStart >= 0 && selectionEnd >= 0) min(selectionStart, selectionEnd) else -1
        val maxSel = if (selectionStart >= 0 && selectionEnd >= 0) max(selectionStart, selectionEnd) else -1

        for (r in 0 until numRows) {
            val top = headerHeight + r * cellHeight
            val rowAddress = r * bytesPerRow

            // Draw row address header
            canvas.drawRect(0f, top, headerWidth, top + cellHeight, headerBgPaint)
            val addrText = String.format("%04X", rowAddress)
            canvas.drawText(addrText, headerWidth / 2f, top + cellHeight * 0.65f, headerTextPaint)
            canvas.drawLine(headerWidth, top, headerWidth, top + cellHeight, gridPaint)
            canvas.drawLine(0f, top + cellHeight, headerWidth + bytesPerRow * cellWidth, top + cellHeight, gridPaint)

            // Draw cells
            for (c in 0 until bytesPerRow) {
                val index = rowAddress + c
                val left = headerWidth + c * cellWidth
                val right = left + cellWidth
                val bottom = top + cellHeight

                if (index in minSel..maxSel) {
                    canvas.drawRect(left, top, right, bottom, selectionPaint)
                }

                if (index < data.size) {
                    val valueStr = String.format("%02X", data[index].toInt() and 0xFF)
                    canvas.drawText(valueStr, left + cellWidth / 2f, top + cellHeight * 0.65f, textPaint)
                }

                canvas.drawLine(right, top, right, bottom, gridPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                if (x >= headerWidth && y >= headerHeight) {
                    val col = ((x - headerWidth) / cellWidth).toInt()
                    val row = ((y - headerHeight) / cellHeight).toInt()
                    val index = row * bytesPerRow + col
                    if (index in data.indices) {
                        if (selectionStart == -1) {
                            selectionStart = index
                        }
                        selectionEnd = index
                        invalidate()
                        onSelectionChangedListener?.invoke(
                            min(selectionStart, selectionEnd),
                            max(selectionStart, selectionEnd)
                        )
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (selectionStart != -1 && selectionEnd != -1) {
                    onCellClickListener?.invoke(selectionEnd)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
