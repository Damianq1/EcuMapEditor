package com.winols.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.winols.app.core.BinaryFileManager
import com.winols.app.core.MapDefinition
import com.winols.app.core.DataType
import com.winols.app.databinding.ActivityEditorBinding
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var fileManager: BinaryFileManager
    private lateinit var currentMap: MapDefinition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sampleFile = File(filesDir, "sample.bin")
        fileManager = BinaryFileManager(sampleFile)
        
        currentMap = MapDefinition(
            id = "map_1",
            name = "Injection Duration",
            address = 0x1234,
            rows = 16,
            cols = 16,
            dataType = DataType.UINT16
        )

        setupUI()
    }

    private fun setupUI() {
        binding.textViewMapInfo.text = "Loaded map: ${currentMap.name}"
        
        val success = fileManager.saveChanges()
        if (success) {
            // Changes saved successfully
        }
    }
}
