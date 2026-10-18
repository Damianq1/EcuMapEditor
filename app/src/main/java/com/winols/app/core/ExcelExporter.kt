package com.winols.app.core

import java.io.InputStream
import java.io.OutputStream

enum class DataType {
    UINT8, INT8, UINT16, INT16, UINT32, INT32, FLOAT32
}

fun InputStream.readValue(dataType: DataType, endianness: Endianness): Number {
    val size = when (dataType) {
        DataType.UINT8, DataType.INT8 -> 1
        DataType.UINT16, DataType.INT16 -> 2
        DataType.UINT32, DataType.INT32, DataType.FLOAT32 -> 4
    }
    val bytes = ByteArray(size)
    this.read(bytes)
    return parseBytes(bytes, dataType, endianness)
}

fun OutputStream.writeValue(value: Number, dataType: DataType, endianness: Endianness) {
    val bytes = formatBytes(value, dataType, endianness)
    this.write(bytes)
}

private fun parseBytes(bytes: ByteArray, dataType: DataType, endianness: Endianness): Number {
    val adjustedBytes = if (endianness == Endianness.LITTLE_ENDIAN) bytes.reversedArray() else bytes
    return when (dataType) {
        DataType.UINT8 -> adjustedBytes[0].toInt() and 0xFF
        DataType.INT8 -> adjustedBytes[0].toInt()
        DataType.UINT16 -> {
            var v = 0
            for (b in adjustedBytes) {
                v = (v shl 8) or (b.toInt() and 0xFF)
            }
            v
        }
        DataType.INT16 -> {
            var v = 0
            for (b in adjustedBytes) {
                v = (v shl 8) or (b.toInt() and 0xFF)
            }
            v.toShort()
        }
        DataType.UINT32 -> {
            var v = 0L
            for (b in adjustedBytes) {
                v = (v shl 8) or (b.toLong() and 0xFF)
            }
            v
        }
        DataType.INT32 -> {
            var v = 0
            for (b in adjustedBytes) {
                v = (v shl 8) or (b.toInt() and 0xFF)
            }
            v
        }
        DataType.FLOAT32 -> {
            var intBits = 0
            for (b in adjustedBytes) {
                intBits = (intBits shl 8) or (b.toInt() and 0xFF)
            }
            Float.fromBits(intBits)
        }
    }
}

private fun formatBytes(value: Number, dataType: DataType, endianness: Endianness): ByteArray {
    val bytes = when (dataType) {
        DataType.UINT8, DataType.INT8 -> byteArrayOf(value.toByte())
        DataType.UINT16, DataType.INT16 -> {
            val v = value.toInt()
            byteArrayOf((v ushr 8).toByte(), v.toByte())
        }
        DataType.UINT32, DataType.INT32 -> {
            val v = value.toInt()
            byteArrayOf((v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte())
        }
        DataType.FLOAT32 -> {
            val intBits = java.lang.Float.floatToIntBits(value.toFloat())
            byteArrayOf((intBits ushr 24).toByte(), (intBits ushr 16).toByte(), (intBits ushr 8).toByte(), intBits.toByte())
        }
    }
    return if (endianness == Endianness.LITTLE_ENDIAN) bytes.reversedArray() else bytes
}

class ExcelExporter(
    private val rawToPhysical: (Double) -> Double = { it },
    private val endianness: Endianness = Endianness.BIG_ENDIAN
) {
    fun exportMap(data: ByteArray, dataType: DataType): String {
        val sb = StringBuilder()
        val stream = data.inputStream()
        try {
            while (stream.available() > 0) {
                val rawVal = stream.readValue(dataType, endianness)
                val physicalVal = rawToPhysical(rawVal.toDouble())
                sb.append(physicalVal).append("\n")
            }
        } finally {
            stream.close()
        }
        return sb.toString()
    }
}
