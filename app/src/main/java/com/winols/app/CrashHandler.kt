package com.winols.app

import android.content.Intent
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler(private val application: android.app.Application) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTraceString = sw.toString()

            val intent = Intent(application, ErrorActivity::class.java).apply {
                putExtra("CRASH_DETAILS", stackTraceString)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            application.startActivity(intent)

            Thread.sleep(2000)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}
