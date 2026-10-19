package com.winols.app.core

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val TAG = "FileLogger"
    private var currentProgramFolder: File? = null
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /**
     * Inicjalizuje logger dla konkretnego programu/pliku binarnego.
     * Tworzy dedykowany folder w pamięci urządzenia o nazwie programu oraz plik logu z znacznikiem czasowym.
     */
    fun init(context: Context, programName: String) {
        try {
            // Czyszczenie nazwy programu z niedozwolonych znaków systemowych
            val safeName = programName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            
            // Katalog bazowy aplikacji w pamięci zewnętrznej (dostępny bez specjalnych uprawnień na minSdk 26+)
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            currentProgramFolder = File(baseDir, "EcuMapEditorLogs/$safeName").apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            logFile = File(currentProgramFolder, "session_$timeStamp.log")
            
            log("System", "FileLogger zainicjalizowany dla projektu/programu: $programName")
            log("System", "Ścieżka do logów: ${logFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas inicjalizacji FileLogger", e)
        }
    }

    /**
     * Zapisuje komunikat zarówno do standardowego Logcata, jak i do pliku tekstowego na urządzeniu.
     */
    fun log(tag: String, message: String) {
        val timeString = dateFormat.format(Date())
        val logEntry = "[$timeString] [$tag]: $message\n"
        
        // Zapis do standardowego Logcata
        Log.d(tag, message)

        // Zapis asynchroniczny lub bezpośredni do pliku sesji
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { fw ->
                    fw.append(logEntry)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd zapisu do pliku logu", e)
        }
    }

    /**
     * Zwraca pełną ścieżkę do aktualnego pliku logów.
     */
    fun getLogFilePath(): String {
        return logFile?.absolutePath ?: "Nie zainicjalizowano"
    }

    /**
     * Zwraca katalog, w którym zapisywane są logi dla edytowanego programu.
     */
    fun getLogDirectory(): File? {
        return currentProgramFolder
    }
}
