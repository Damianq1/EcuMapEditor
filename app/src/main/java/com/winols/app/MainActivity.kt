package com.winols.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.winols.app.core.FileLogger
import com.winols.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val currentProgramName = "EDC15P+_BMW_320d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfiguracja Toolbara jako paska akcji
        setSupportActionBar(binding.toolbar)

        // Inicjalizacja loggera dla projektu
        FileLogger.init(this, currentProgramName)
        
        val logPath = FileLogger.getLogFilePath()
        binding.textLogPath.text = "Logi: $logPath"
        appendConsole("Zainicjalizowano sesję dla projektu: $currentProgramName")
        FileLogger.log("MainActivity", "Aktywność główna uruchomiona pomyślnie.")

        // Obsługa przycisków w UI
        binding.btnScanMaps.setOnClickListener {
            appendConsole("[MapFinderEngine]: Rozpoczęto skanowanie strumienia bajtów...")
            FileLogger.log("MapFinder", "Uruchomiono heurystyczne skanowanie struktur.")
            Toast.NotFoundHandler?.let {} ?: Toast.makeText(this, "Skanowanie w tle...", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenGridEditor.setOnClickListener {
            appendConsole("[GridEditor]: Ładowanie widoku siarki macierzy 2D/3D...")
            FileLogger.log("GridEditor", "Otwarto widok edytora macierzy.")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_binary -> {
                appendConsole("[BinaryIO]: Otwieranie eksploratora plików .bin / .hex...")
                FileLogger.log("Menu", "Wybrano opcję otwarcia pliku binarnego.")
                true
            }
            R.id.action_view_logs -> {
                appendConsole("[FileLogger]: Ścieżka pliku logu:\n${FileLogger.getLogFilePath()}")
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Ustawienia map", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun appendConsole(message: String) {
        val currentText = binding.textConsoleOutput.text.toString()
        binding.textConsoleOutput.text = "$currentText\n$message"
    }
}
