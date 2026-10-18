package com.winols.app.core

import java.io.InputStream
import java.io.OutputStream

class ChecksumEngine {
    fun process(inputStream: InputStream, outputStream: OutputStream, dataType: DataType, endianness: Endianness) {
        try {
            val value = inputStream.readValue(dataType, endianness)
            val adjusted = when (dataType) {
                DataType.UINT8 -> (value.toInt() + 1) and 0xFF
                DataType.INT8 -> (value.toInt() + 1).toByte()
                DataType.UINT16 -> (value.toInt() + 1) and 0xFFFF
                DataType.INT16 -> (value.toInt() + 1).toShort()
                DataType.UINT32 -> (value.toLong() + 1L) and 0xFFFFFFFFL
                DataType.INT32 -> value.toInt() + 1
                DataType.FLOAT32 -> value.toFloat() + 1.0f
            }
            outputStream.writeValue(adjusted, dataType, endianness)
        } catch (e: Exception) {
            // Handle error or EOF
        }
    }
}
