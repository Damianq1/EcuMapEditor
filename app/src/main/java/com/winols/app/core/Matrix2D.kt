package com.winols.app.core

data class Matrix2D(
    val rows: Int,
    val cols: Int,
    val data: FloatArray,
    val xAxis: FloatArray = floatArrayOf(),
    val yAxis: FloatArray = floatArrayOf()
) {
    fun get(row: Int, col: Int): Float {
        return data[row * cols + col]
    }

    fun set(row: Int, col: Int, value: Float) {
        data[row * cols + col] = value
    }

    fun fill(value: Float) {
        data.fill(value)
    }

    fun add(value: Float) {
        for (i in data.indices) {
            data[i] += value
        }
    }

    fun scale(percentage: Float) {
        val multiplier = 1.0f + (percentage / 100.0f)
        for (i in data.indices) {
            data[i] *= multiplier
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Matrix2D
        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (!data.contentEquals(other.data)) return false
        if (!xAxis.contentEquals(other.xAxis)) return false
        if (!yAxis.contentEquals(other.yAxis)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + data.contentHashCode()
        result = 31 * result + xAxis.contentHashCode()
        result = 31 * result + yAxis.contentHashCode()
        return result
    }
}
