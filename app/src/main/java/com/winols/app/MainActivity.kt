package com.winols.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.winols.app.databinding.ActivityMainBinding
import com.winols.app.ui.EditorActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleSelectedFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnOpenBinFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnDemoProject.setOnClickListener {
            createAndOpenDemoFile()
        }
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val cacheFile = File(cacheDir, "imported_ecu_file.bin")
            val outputStream = FileOutputStream(cacheFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            openEditor(cacheFile)
        } catch (e: Exception) {
            Toast.makeText(this, "Błąd podczas otwierania pliku: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createAndOpenDemoFile() {
        try {
            val demoFile = File(cacheDir, "demo_ecu_file.bin")
            val dummyData = ByteArray(1024 * 64) { (it % 256).toByte() }
            demoFile.writeBytes(dummyData)
            openEditor(demoFile)
        } catch (e: Exception) {
            Toast.makeText(this, "Błąd tworzenia pliku demo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openEditor(file: File) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra("FILE_PATH", file.absolutePath)
        }
        startActivity(intent)
    }
}
