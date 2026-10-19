package com.winols.app.core

import java.io.File

class ChecksumEngine {

    fun processFile(file: File) {
        // Corrected from readValue to standard Kotlin file reading readBytes()
        val data = file.readBytes()

        // Perform checksum calculation or modifications on `data` bytes here...

        // Corrected from writeValue to standard Kotlin file writing writeBytes()
        file.writeBytes(data)
    }
}
