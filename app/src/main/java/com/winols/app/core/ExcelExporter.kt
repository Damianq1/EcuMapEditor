package com.winols.app.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ExcelExporter(private val context: Context) {

    /**
     * Exports map data (or differences between original and modified bytes) to a CSV format file.
     * Can be easily imported into Microsoft Excel, LibreOffice Calc, or Google Sheets.
     */
    suspend fun exportMapToCsv(
        fileName: String,
        mapDefinition: MapDefinition,
        originalManager: BinaryFileManager?,
        modifiedManager: BinaryFileManager
    ): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, fileName)
        
        FileWriter(outputFile).use { writer ->
            // Header information
            writer.append("Map ID,${mapDefinition.id}\n")
            writer.append("Map Name,${mapDefinition.name}\n")
            writer.append("Start Address,0x${Integer.toHexString(mapDefinition.startAddress).uppercase()}\n")
            writer.append("Rows,${mapDefinition.rows}\n")
            writer.append("Cols,${mapDefinition.cols}\n\n")

            // Read X-Axis values if applicable
            val xValues = mutableListOf<Double>()
            if (mapDefinition.xAxisAddress >= 0) {
                val xLength = mapDefinition.cols * 2 // Assuming UINT16 for axes usually
                try {
                    val xBytes = modifiedManager.readBytes(mapDefinition.xAxisAddress, xLength)
                    val xBuffer = java.nio.ByteBuffer.wrap(xBytes).order(mapDefinition.endianness.toJavaOrder())
                    for (i in 0 until mapDefinition.cols) {
                        val rawVal = when (mapDefinition.dataType) {
                            DataType.UINT8 -> xBuffer.get().toInt() and 0xFF
                            DataType.INT8 -> xBuffer.get().toInt()
                            DataType.UINT16 -> xBuffer.short.toInt() and 0xFFFF
                            DataType.INT16 -> xBuffer.short.toInt()
                            DataType.UINT32 -> xBuffer.int.toLong() and 0xFFFFFFFFL
                            DataType.INT32 -> xBuffer.int
                        }
                        xValues.add(mapDefinition.rawXToPhysical(rawVal))
                    }
                } catch (e: Exception) {
                    // Fallback to indices if axis reading fails
                    for (i in 0 until mapDefinition.cols) xValues.add(i.toDouble())
                }
            } else {
                for (i in 0 until mapDefinition.cols) xValues.add(i.toDouble())
            }

            // Read Y-Axis values if applicable
            val yValues = mutableListOf<Double>()
            if (mapDefinition.yAxisAddress >= 0) {
                val yLength = mapDefinition.rows * 2
                try {
                    val yBytes = modifiedManager.readBytes(mapDefinition.yAxisAddress, yLength)
                    val yBuffer = java.nio.ByteBuffer.wrap(yBytes).order(mapDefinition.endianness.toJavaOrder())
                    for (i in 0 until mapDefinition.rows) {
                        val rawVal = when (mapDefinition.dataType) {
                            DataType.UINT8 -> yBuffer.get().toInt() and 0xFF
                            DataType.INT8 -> yBuffer.get().toInt()
                            DataType.UINT16 -> yBuffer.short.toInt() and 0xFFFF
                            DataType.INT16 -> yBuffer.short.toInt()
                            DataType.UINT32 -> yBuffer.int.toLong() and 0xFFFFFFFFL
                            DataType.INT32 -> yBuffer.int
                        }
                        yValues.add(mapDefinition.rawYToPhysical(rawVal))
                    }
                } catch (e: Exception) {
                    for (i in 0 until mapDefinition.rows) yValues.add(i.toDouble())
                }
            } else {
                for (i in 0 until mapDefinition.rows) yValues.add(i.toDouble())
            }

            // Write CSV Table Header (X-Axis values)
            writer.append("Y\\X")
            for (xVal in xValues) {
                writer.append(",$xVal")
            }
            writer.append("\n")

            // Calculate element size in bytes
            val elementSize = when (mapDefinition.dataType) {
                DataType.UINT8, DataType.INT8 -> 1
                DataType.UINT16, DataType.INT16 -> 2
                DataType.UINT32, DataType.INT32 -> 4
            }

            // Write Data Rows
            for (r in 0 until mapDefinition.rows) {
                val yLabel = if (yValues.size > r) yValues[r].toString() else r.toString()
                writer.append(yLabel)

                for (c in 0 until mapDefinition.cols) {
                    val cellOffset = mapDefinition.startAddress + (r * mapDefinition.cols + c) * elementSize
                    
                    val modRaw = readRawValue(modifiedManager, cellOffset, mapDefinition.dataType, mapDefinition.endianness)
                    val modPhysical = mapDefinition.rawToPhysical(modRaw)

                    if (originalManager != null) {
                        val oriRaw = readRawValue(originalManager, cellOffset, mapDefinition.dataType, mapDefinition.endianness)
                        val oriPhysical = mapDefinition.rawToPhysical(oriRaw)
                        val diff = modPhysical - oriPhysical
                        // Output format: ModVal (Diff: delta)
                        writer.append(",$modPhysical (diff: $diff)")
                    } else {
                        writer.append(",$modPhysical")
                    }
                }
                writer.append("\n")
            }
        }

        outputFile
    }

    private fun readRawValue(manager: BinaryFileManager, offset: Int, type: DataType, endianness: Endianness): Number {
        return try {
            manager.readValue(offset, type, endianness)
        } catch (e: Exception) {
            0
        }
    }

    private fun Endianness.toJavaOrder(): java.nio.ByteOrder {
        return when (this) {
            Endianness.LITTLE_ENDIAN -> java.nio.ByteOrder.LITTLE_ENDIAN
            Endianness.BIG_ENDIAN -> java.nio.ByteOrder.BIG_ENDIAN
        }
    }
}
