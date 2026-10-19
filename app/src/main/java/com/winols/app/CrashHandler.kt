package com.winols.app

import android.os.Environment
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTraceString = sw.toString()

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val crashFile = File(downloadsDir, "EcuMapEditor_Crash.txt")
            crashFile.writeText("=== ECU MAP EDITOR CRASH REPORT ===\n" +
                    "Data: ${System.currentTimeMillis()}\n\n" +
                    "$stackTraceString")

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e)
        } else {
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
}
