package com.AppCore.app.core

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LoggerManager {
    private const val TAG = "EcuMapEditorLogger"
    private const val LOG_FILE_NAME = "ecu_editor_crash.log"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var logFile: File? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val dir = context.filesDir
            logFile = File(dir, LOG_FILE_NAME)
            
            // Setup uncaught exception handler to catch startup crashes
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    writeLogSync("CRASH on thread ${thread.name}: ${throwable.stackTraceToString()}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write crash log", e)
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }

            isInitialized = true
            writeLog("Logger initialized successfully. minSdk=26 active.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LoggerManager", e)
        }
    }

    fun writeLog(message: String) {
        scope.launch {
            writeLogSync(message)
        }
    }

    private fun writeLogSync(message: String) {
        try {
            val file = logFile ?: return
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            val formattedMessage = "[$timestamp] $message\n"
            
            FileWriter(file, true).use { fw ->
                PrintWriter(fw).use { pw ->
                    pw.print(formattedMessage)
                }
            }
            Log.d(TAG, message)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file", e)
        }
    }

    fun getLogFileContent(): String {
        return try {
            val file = logFile ?: return "Log file not initialized."
            if (file.exists()) {
                file.readText()
            } else {
                "Log file does not exist yet."
            }
        } catch (e: Exception) {
            "Error reading log file: ${e.message}"
        }
    }
}
