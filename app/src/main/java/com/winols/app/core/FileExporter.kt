package com.winols.app.core

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileExporter {

    /**
     * Kopiuje plik logu lub eksportowany zrzut do pamięci publicznej (Downloads) 
     * oraz uruchamia systemowy Intent udostępniania (Export/Share).
     */
    fun exportFile(context: Context, sourceFile: File, mimeType: String = "text/plain") {
        try {
            if (!sourceFile.exists()) {
                FileLogger.log("FileExporter", "Plik źródłowy nie istnieje: ${sourceFile.absolutePath}")
                return
            }

            // Udostępnianie pliku przez FileProvider zgodne z Android 7.0+ (minSdk 26+)
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, sourceFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "EcuMapEditor - Eksport: ${sourceFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Eksportuj plik przez...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            FileLogger.log("FileExporter", "Zainicjowano eksport pliku: ${sourceFile.name}")
        } catch (e: Exception) {
            FileLogger.log("FileExporter", "Błąd podczas eksportu pliku: ${e.message}")
        }
    }
}
