package com.winols.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapFinderEngine(private val binaryFileManager: BinaryFileManager) {

    suspend fun findPotentialMaps(): List<MapDefinition> = withContext(Dispatchers.Default) {
        val maps = mutableListOf<MapDefinition>()
        val size = binaryFileManager.size()
        if (size <= 0) return@withContext maps

        val standardSizes = listOf(
            Pair(8, 8),
            Pair(16, 16),
            Pair(10, 10),
            Pair(12, 1),
            Pair(16, 1),
            Pair(8, 1)
        )

        var mapIdCounter = 1
        var address = 0

        while (address < size - 128) {
            var found = false
            for ((cols, rows) in standardSizes) {
                val dataSize = cols * rows * 2
                val xAxisSize = cols * 2
                val yAxisSize = rows * 2
                val totalNeeded = xAxisSize + yAxisSize + dataSize

                if (address + totalNeeded < size) {
                    if (isBlockInteresting(address, cols, rows)) {
                        val xAxisAddr = address
                        val yAxisAddr = xAxisAddr + xAxisSize
                        val dataAddr = yAxisAddr + yAxisSize

                        val mapDef = MapDefinition(
                            id = "map_$mapIdCounter",
                            name = "Auto Map ${mapIdCounter} (${cols}x${rows}) [0x${dataAddr.toString(16).uppercase()}]",
                            startAddress = dataAddr,
                            xAxisAddress = xAxisAddr,
                            yAxisAddress = yAxisAddr,
                            cols = cols,
                            rows = rows,
                            dataType = DataType.UINT16,
                            endianness = Endianness.LITTLE_ENDIAN,
                            factor = 1.0,
                            offset = 0.0,
                            xAxisFactor = 1.0,
                            xAxisOffset = 0.0,
                            yAxisFactor = 1.0,
                            yAxisOffset = 0.0
                        )
                        maps.add(mapDef)
                        mapIdCounter++
                        address = dataAddr + dataSize
                        found = true
                        break
                    }
                }
            }
            if (!found) {
                address += 4
            }
        }

        return@withContext maps
    }

    private fun isBlockInteresting(startAddress: Int, cols: Int, rows: Int): Boolean {
        return try {
            val length = cols * rows * 2
            val bytes = binaryFileManager.readBytes(startAddress, length)
            if (bytes.isEmpty()) return false

            val allZeros = bytes.all { it == 0.toByte() }
            val allOnes = bytes.all { it == 0xFF.toByte() }
            if (allZeros || allOnes) return false

            val uniqueBytes = bytes.take(minOf(bytes.size, 64)).toSet()
            uniqueBytes.size > 2
        } catch (e: Exception) {
            false
        }
    }
}
