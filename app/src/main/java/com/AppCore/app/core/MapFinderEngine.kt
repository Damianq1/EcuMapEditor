package com.AppCore.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MapDefinition(
    val name: String,
    val address: Int,
    val rows: Int,
    val cols: Int,
    val xAxisAddress: Int,
    val yAxisAddress: Int,
    val factor: Float,
    val offset: Float
)

class MapFinderEngine {

    suspend fun scanForMaps(bufferEngine: BinaryBufferEngine): List<MapDefinition> = withContext(Dispatchers.Default) {
        val discoveredMaps = mutableListOf<MapDefinition>()
        val capacity = bufferEngine.capacity()
        
        // Simple heuristic search example for standard map signatures or grid headers
        // In real ECU environment, this would search for specific magic bytes or axis patterns
        var index = 0
        while (index < capacity - 16) {
            // Example heuristic: looking for a specific marker or patterned header
            val potentialMarker = bufferEngine.getShort(index)
            if (potentialMarker == 0x434D.toShort()) { // 'MC' marker example
                try {
                    val rows = bufferEngine.getByte(index + 2).toInt() and 0xFF
                    val cols = bufferEngine.getByte(index + 3).toInt() and 0xFF
                    if (rows in 2..32 && cols in 2..32) {
                        discoveredMaps.add(
                            MapDefinition(
                                name = "Map_${index.toString(16).uppercase()}",
                                address = index + 4,
                                rows = rows,
                                cols = cols,
                                xAxisAddress = index + 6,
                                yAxisAddress = index + 8,
                                factor = 0.01f,
                                offset = 0.0f
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore out of bounds during heuristic scan
                }
            }
            index += 2
        }
        discoveredMaps
    }

    fun convertRawToPhysical(raw: Int, factor: Float, offset: Float): Types.FLOAT32 {
        return (raw * factor) + offset
    }

    fun convertPhysicalToRaw(physical: Float, factor: Float, offset: Float): Int {
        return if (factor != 0f) ((physical - offset) / factor).toInt() else 0
    }
}
