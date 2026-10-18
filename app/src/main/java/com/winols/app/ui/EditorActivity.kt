package com.winols.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.winols.app.core.BinaryFileManager
import com.winols.app.core.ChecksumEngine
import com.winols.app.core.DataType
import com.winols.app.core.Endianness
import com.winols.app.core.ExcelExporter
import com.winols.app.core.MapDefinition
import com.winols.app.core.MapFinderEngine
import com.winols.app.databinding.ActivityEditorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var binaryFileManager: BinaryFileManager
    private lateinit var checksumEngine: ChecksumEngine
    private lateinit var excelExporter: ExcelExporter
    private lateinit var mapFinderEngine: MapFinderEngine

    private var currentMap: MapDefinition? = null
    private var loadedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binaryFileManager = BinaryFileManager()
        checksumEngine = ChecksumEngine(binaryFileManager)
        excelExporter = ExcelExporter(this)
        mapFinderEngine = MapFinderEngine(binaryFileManager)

        val filePath = intent.getStringExtra("FILE_PATH")
        if (filePath != null) {
            loadedFile = File(filePath)
            loadBinaryFile(loadedFile!!)
        } else {
            Toast.makeText(this, "Nie wybrano pliku", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupListeners()
    }

    private fun loadBinaryFile(file: File) {
        lifecycleScope.launch {
            try {
                binaryFileManager.loadFile(file)
                Toast.getInstance(this@EditorActivity, "Załadowano plik: ${file.name}", Toast.LENGTH_SHORT).show()
                detectMaps()
            } catch (e: Exception) {
                Toast.makeText(this@EditorActivity, "Błąd ładowania pliku: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun detectMaps() {
        lifecycleScope.launch {
            val maps = mapFinderEngine.findPotentialMaps()
            if (maps.isNotEmpty()) {
                currentMap = maps[0]
                displayMapData(currentMap!!)
            } else {
                Toast.makeText(this@EditorActivity, "Nie wykryto automatycznych map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMapData(mapDef: MapDefinition) {
        lifecycleScope.launch {
            val length = mapDef.cols * mapDef.rows * when (mapDef.dataType) {
                DataType.UINT8, DataType.INT8 -> 1
                else -> 2
            }
            val dataBytes = binaryFileManager.readBytes(mapDef.startAddress, length)
            
            withContext(Dispatchers.Main) {
                binding.hexGridView.setData(dataBytes, mapDef.cols)
                
                // Konwersja na FloatArray dla wykresów 2D/3D
                val floatValues = FloatArray(dataBytes.size / 2) { i ->
                    val raw = java.nio.ByteBuffer.wrap(dataBytes)
                        .order(mapDef.endianness.toJavaOrder())
                        .getShort(i * 2).toInt() and 0xFFFF
                    mapDef.rawToPhysical(raw).toFloat()
                }
                
                val xVals = FloatArray(mapDef.cols) { it.toFloat() }
                binding.waveform2DView.setData(xVals, floatValues.take(mapDef.cols).toFloatArray())

                val grid2D = Array(mapDef.rows) { r ->
                    FloatArray(mapDef.cols) { c ->
                        val index = (r * mapDef.cols + c) * 2
                        if (index + 2 <= dataBytes.size) {
                            val raw = java.nio.ByteBuffer.wrap(dataBytes)
                                .order(mapDef.endianness.toJavaOrder())
                                .getShort(index).toInt() and 0xFFFF
                            mapDef.rawToPhysical(raw).toFloat()
                        } else 0f
                    }
                }
                binding.surface3DView.setData(grid2D)
            }
        }
    }

    private fun setupListeners() {
        binding.btnApplyConstant.setOnClickListener {
            binding.hexGridView.applyConstant(10)
            updateViewFromGrid()
        }

        binding.btnApplyPercentage.setOnClickListener {
            binding.hexGridView.applyPercentage(5.0)
            updateViewFromGrid()
        }

        binding.btnExportCsv.setOnClickListener {
            currentMap?.let { map ->
                lifecycleScope.launch {
                    val fileName = "map_export_${System.currentTimeMillis()}.csv"
                    val success = excelExporter.exportMapToCsv(map, binaryFileManager, binaryFileManager, fileName)
                    withContext(Dispatchers.Main) {
                        val msg = if (success) "Eksport zakończony sukcesem" else "Błąd eksportu"
                        Toast.makeText(this@EditorActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    binaryFileManager.saveFile()
                    Toast.makeText(this@EditorActivity, "Zapisano pomyślnie", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@EditorActivity, "Błąd zapisu: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateViewFromGrid() {
        currentMap?.let { map ->
            val updatedBytes = binding.hexGridView.getData()
            lifecycleScope.launch {
                binaryFileManager.writeBytes(map.startAddress, updatedBytes)
                displayMapData(map)
            }
        }
    }

    private fun Endianness.toJavaOrder(): java.nio.ByteOrder {
        return when (this) {
            Endianness.LITTLE_ENDIAN -> java.nio.ByteOrder.LITTLE_ENDIAN
            Endianness.BIG_ENDIAN -> java.nio.ByteOrder.BIG_ENDIAN
        }
    }
}
