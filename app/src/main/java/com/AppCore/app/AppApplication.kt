package com.AppCore.app

import android.app.Application
import com.AppCore.app.core.LoggerManager

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize logger first thing on app launch to catch early startup exceptions
        LoggerManager.init(this)
        LoggerManager.writeLog("AppApplication onCreate started.")
    }
}
