package com.winols.app.core

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun OutputStream.writeValue(value: Number, dataType: DataType, endianness: Endianness) {
    val order = if (endianness == Endianness.BIG_ENDIAN) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
    val bytes = when (dataType) {
        DataType.UINT8, DataType.INT8 -> byteArrayOf(value.toByte())
        DataType.UINT16, DataType.INT16 -> ByteBuffer.allocate(2).order(order).putShort(value.toShort()).array()
        DataType.UINT32, DataType.INT32 -> ByteBuffer.allocate(4).order(order).putInt(value.toInt()).array()
        DataType.FLOAT32 -> ByteBuffer.allocate(4).order(order).putFloat(value.toFloat()).array()
    }
    write(bytes)
}

fun InputStream.readValue(dataType: DataType, endianness: Endianness): Number {
    val order = if (endianness == Endianness.BIG_ENDIAN) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
    val size = when (dataType) {
        DataType.UINT8, DataType.INT8 -> 1
        DataType.UINT16, DataType.INT16 -> 2
        DataType.UINT32, DataType.INT32, DataType.FLOAT32 -> 4
    }
    val buffer = ByteArray(size)
    read(buffer)
    val bb = ByteBuffer.wrap(buffer).order(order)
    return when (dataType) {
        DataType.UINT8 -> buffer[0].toInt() and 0xFF
        DataType.INT8 -> buffer[0]
        DataType.UINT16 -> bb.short.toInt() and 0xFFFF
        DataType.INT16 -> bb.short
        DataType.UINT32 -> bb.int.toLong() and 0xFFFFFFFFL
        DataType.INT32 -> bb.int
        DataType.FLOAT32 -> bb.float
    }
}

class ExcelExporter {
    fun exportData(outputStream: OutputStream, data: List<Number>, dataType: DataType, endianness: Endianness) {
        for (item in data) {
            outputStream.writeValue(item, dataType, endianness)
        }
    }
}
