package com.AppCore.app.core

import com.AppCore.app.model.Types

class BinaryBufferEngine {
    // Corrected buffer operations using Types model
    fun readByte(buffer: ByteArray, offset: Int): Byte {
        return buffer[offset]
    }

    fun readInt(buffer: ByteArray, offset: Int): Int {
        return ((buffer[offset].toInt() and 0xFF) shl 24) or
               ((buffer[offset + 1].toInt() and 0xFF) shl 16) or
               ((buffer[offset + 2].toInt() and 0xFF) shl 8) or
               (buffer[offset + 3].toInt() and 0xFF)
    }
}
