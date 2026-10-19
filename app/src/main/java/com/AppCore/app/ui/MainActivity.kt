package com.AppCore.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.AppCore.app.core.LoggerManager
import com.AppCore.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            LoggerManager.writeLog("MainActivity onCreate initiated.")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupUI()
            LoggerManager.writeLog("MainActivity UI successfully inflated and initialized.")
        } catch (e: Exception) {
            LoggerManager.writeLog("CRITICAL ERROR in MainActivity onCreate: ${e.stackTraceToString()}")
            Toast.makeText(this, "Startup Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        binding.tvWelcome.text = "EcuMapEditor Core v1.0 Active"
        binding.btnViewLogs.setOnClickListener {
            val logs = LoggerManager.getLogFileContent()
            // In a full implementation, display this in a Dialog or secondary Fragment/Activity
            Toast.dump(this, "Logs dumped to Logcat", arrayOf(logs), 0)
            LoggerManager.writeLog("User requested log review. Length: ${logs.length}")
        }
    }
}
