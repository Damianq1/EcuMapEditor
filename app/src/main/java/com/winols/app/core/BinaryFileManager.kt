package com.winols.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

enum class Endianness {
    LITTLE_ENDIAN,
    BIG_ENDIAN
}

enum class DataType {
    INT8,
    UINT8,
    INT16,
    UINT16
}

class BinaryFileManager {

    private var fileBuffer: ByteBuffer? = null
    private var sourceFile: File? = null
    private var isModified: Boolean = false

    suspend fun loadFile(file: File) = withContext(Dispatchers.IO) {
        sourceFile = file
        RandomAccessFile(file, "r").use { raf ->
            raf.channel.use { channel ->
                val size = channel.size()
                val buffer = ByteBuffer.allocate(size.toInt())
                channel.read(buffer)
                buffer.flip()
                fileBuffer = buffer
                isModified = false
            }
        }
    }

    suspend fun saveFile(destination: File? = null) = withContext(Dispatchers.IO) {
        val buffer = fileBuffer ?: throw IllegalStateException("No file loaded in memory")
        val target = destination ?: sourceFile ?: throw IllegalStateException("No destination file specified")
        
        buffer.rewind()
        RandomAccessFile(target, "rw").use { raf ->
            raf.channel.use { channel ->
                channel.write(buffer)
                channel.force(false)
            }
        }
        isModified = false
    }

    fun size(): Int {
        return fileBuffer?.capacity() ?: 0
    }

    fun isDataModified(): Boolean {
        return isModified
    }

    fun readValue(offset: Int, type: DataType, endianness: Endianness): Number {
        val buffer = fileBuffer ?: throw IllegalStateException("Buffer not initialized")
        buffer.order(if (endianness == Endianness.LITTLE_ENDIAN) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN)
        
        return when (type) {
            DataType.INT8 -> {
                buffer.get(offset).toInt()
            }
            DataType.UINT8 -> {
                buffer.get(offset).toInt() and 0xFF
            }
            DataType.INT16 -> {
                buffer.getShort(offset).toInt()
            }
            DataType.UINT16 -> {
                buffer.getShort(offset).toInt() and 0xFFFF
            }
        }
    }

    fun writeValue(offset: Int, value: Number, type: DataType, endianness: Endianness) {
        val buffer = fileBuffer ?: throw IllegalStateException("Buffer not initialized")
        buffer.order(if (endianness == Endianness.LITTLE_ENDIAN) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN)

        when (type) {
            DataType.INT8, DataType.UINT8 -> {
                buffer.put(offset, value.toByte())
            }
            DataType.INT16, DataType.UINT16 -> {
                buffer.putShort(offset, value.toShort())
            }
        }
        isModified = true
    }

    fun readBytes(offset: Int, length: Int): ByteArray {
        val buffer = fileBuffer ?: throw IllegalStateException("Buffer not initialized")
        val dst = ByteArray(length)
        val duplicate = buffer.duplicate()
        duplicate.position(offset)
        duplicate.get(dst)
        return dst
    }

    fun writeBytes(offset: Int, src: ByteArray) {
        val buffer = fileBuffer ?: throw IllegalStateException("Buffer not initialized")
        val duplicate = buffer.duplicate()
        duplicate.position(offset)
        duplicate.put(src)
        isModified = true
    }
}
