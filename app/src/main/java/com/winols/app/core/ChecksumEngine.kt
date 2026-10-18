package com.winols.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChecksumEngine(private val binaryFileManager: BinaryFileManager) {

    suspend fun calculateChecksum(startAddress: Int, length: Int, algorithm: String = "SUM"): Long = withContext(Dispatchers.Default) {
        val bytes = binaryFileManager.readBytes(startAddress, length)
        when (algorithm.uppercase()) {
            "SUM", "ADDITIVE" -> {
                var sum = 0L
                for (b in bytes) {
                    sum += (b.toInt() and 0xFF)
                }
                sum and 0xFFFFFFFFL
            }
            "CRC32" -> {
                var crc = 0xFFFFFFFFL
                for (b in bytes) {
                    val index = ((crc xor b.toLong()) and 0xFF).toInt()
                    crc = (crc ushr 8) xor crcTable[index]
                }
                (crc xor 0xFFFFFFFFL) and 0xFFFFFFFFL
            }
            else -> {
                var sum = 0L
                for (b in bytes) {
                    sum += (b.toInt() and 0xFF)
                }
                sum and 0xFFFFL
            }
        }
    }

    suspend fun verifyAndFixChecksum(checksumAddress: Int, startAddress: Int, length: Int, algorithm: String = "SUM"): Boolean = withContext(Dispatchers.Default) {
        val calculated = calculateChecksum(startAddress, length, algorithm)
        when (algorithm.uppercase()) {
            "SUM", "ADDITIVE" -> {
                binaryFileManager.writeValue(checksumAddress, calculated.toInt(), DataType.UINT16, Endianness.LITTLE_ENDIAN)
            }
            "CRC32" -> {
                binaryFileManager.writeValue(checksumAddress, calculated, DataType.UINT32, Endianness.LITTLE_ENDIAN)
            }
            else -> {
                binaryFileManager.writeValue(checksumAddress, calculated.toInt(), DataType.UINT16, Endianness.LITTLE_ENDIAN)
            }
        }
        true
    }

    companion object {
        private val crcTable: LongArray by lazy {
            val table = LongArray(256)
            for (i in 0 until 256) {
                var c = i.toLong()
                for (j in 0 until 8) {
                    c = if ((c and 1L) != 0L) {
                        0xEDB88320L xor (c ushr 1)
                    } else {
                        c ushr 1
                    }
                }
                table[i] = c
            }
            table
        }
    }
}
