package com.AppCore.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class BinaryBufferEngine {

    private var directBuffer: ByteBuffer? = null

    suspend fun loadFile(file: File, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): Unit = withContext(Dispatchers.IO) {
        val channel = FileChannel.open(file.toPath(), java.nio.file.StandardOpenOption.READ)
        val size = channel.size()
        val buffer = ByteBuffer.allocateDirect(size.toInt())
        channel.use { ch ->
            while (buffer.hasRemaining()) {
                ch.read(buffer)
            }
        }
        buffer.flip()
        buffer.order(byteOrder)
        directBuffer = buffer
    }

    suspend fun saveFile(file: File): Unit = withContext(Dispatchers.IO) {
        val buffer = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        buffer.rewind()
        val channel = FileChannel.open(
            file.toPath(),
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.WRITE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        )
        channel.use { ch ->
            ch.write(buffer)
        }
        buffer.rewind()
    }

    fun getByte(index: Int): Byte {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        return buf.get(index)
    }

    fun setByte(index: Int, value: Byte) {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        buf.put(index, value)
    }

    fun getShort(index: Int): Short {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        return buf.getShort(index)
    }

    fun setShort(index: Int, value: Short) {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        buf.putShort(index, value)
    }

    fun getInt(index: Int): Types.INT32 {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        return buf.getInt(index)
    }

    fun setInt(index: Int, value: Types.INT32) {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        buf.putInt(index, value)
    }

    fun getFloat(index: Int): Types.FLOAT32 {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        return buf.getFloat(index)
    }

    fun setFloat(index: Int, value: Types.FLOAT32) {
        val buf = directBuffer ?: throw IllegalStateException("Buffer is not initialized")
        buf.putFloat(index, value)
    }

    fun capacity(): Int {
        return directBuffer?.capacity() ?: 0
    }
}
