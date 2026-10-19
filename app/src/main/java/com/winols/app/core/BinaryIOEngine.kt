package com.winols.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

enum class Endianness {
    BIG_ENDIAN,
    LITTLE_ENDIAN,
    HI_LO
}

class BinaryIOEngine(private val file: File) {

    private var randomAccessFile: RandomAccessFile? = null
    private var fileChannel: FileChannel? = null
    private var directBuffer: ByteBuffer? = null

    suspend fun open(mode: String = "r") = withContext(Dispatchers.IO) {
        randomAccessFile = RandomAccessFile(file, mode)
        fileChannel = randomAccessFile?.channel
        fileChannel?.let { channel ->
            val mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
            directBuffer = mappedBuffer
        }
    }

    suspend fun readBytes(offset: Int, length: Int): ByteArray = withContext(Dispatchers.IO) {
        val buffer = directBuffer ?: throw IllegalStateException("Buffer not initialized")
        synchronized(buffer) {
            buffer.position(offset)
            val dst = ByteArray(length)
            buffer.get(dst)
            dst
        }
    }

    suspend fun writeBytes(offset: Int, data: ByteArray) = withContext(Dispatchers.IO) {
        val channel = fileChannel ?: throw IllegalStateException("File channel not initialized")
        val writableBuffer = ByteBuffer.allocateDirect(data.size)
        writableBuffer.put(data)
        writableBuffer.flip()
        channel.write(writableBuffer, offset.toLong())
    }

    fun setByteOrder(endianness: Endianness) {
        directBuffer?.order(
            when (endianness) {
                Endianness.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
                Endianness.LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
                Endianness.HI_LO -> ByteOrder.LITTLE_ENDIAN // Custom handling if needed
            }
        )
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        fileChannel?.close()
        randomAccessFile?.close()
        directBuffer = null
    }
}
