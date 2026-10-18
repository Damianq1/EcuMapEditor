package com.winols.app.core

import java.io.File

class BinaryFileManager(private val file: File) {
    var data: ByteArray = byteArrayOf()
        private set

    init {
        if (file.exists()) {
            data = file.readBytes()
        }
    }

    fun saveChanges(): Boolean {
        return try {
            file.writeBytes(data)
            true
        } catch (e: Exception) {
            false
        }
    }
}
