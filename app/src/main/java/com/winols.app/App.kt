package com.winols.app

import android.app.Application
import com.winols.app.core.FileLogger

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicjalizacja globalnego loggera plikowego z domyślnym kontekstem sesji
        FileLogger.init(this, "EcuMapEditor_DefaultSession")
        FileLogger.log("App", "Aplikacja została uruchomiona pomyślnie.")
    }
}
