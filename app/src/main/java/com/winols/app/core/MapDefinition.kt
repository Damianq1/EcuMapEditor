package com.winols.app.core

data class MapDefinition(
    val id: String,
    val name: String,
    val startAddress: Int,
    val xAxisAddress: Int,
    val yAxisAddress: Int,
    val cols: Int,
    val rows: Int,
    val dataType: DataType = DataType.UINT16,
    val endianness: Endianness = Endianness.LITTLE_ENDIAN,
    val factor: Double = 1.0,
    val offset: Double = 0.0,
    val xAxisFactor: Double = 1.0,
    val xAxisOffset: Double = 0.0,
    val yAxisFactor: Double = 1.0,
    val yAxisOffset: Double = 0.0
) {
    fun rawToPhysical(raw: Number): Double {
        return (raw.toDouble() * factor) + offset
    }

    fun physicalToRaw(physical: Double): Number {
        val rawDouble = (physical - offset) / factor
        return when (dataType) {
            DataType.UINT8, DataType.INT8 -> rawDouble.toInt().toByte()
            DataType.UINT16, DataType.INT16 -> rawDouble.toInt().toShort()
            DataType.UINT32, DataType.INT32, DataType.FLOAT32 -> rawDouble.toInt()
        }
    }

    fun rawXToPhysical(raw: Number): Double {
        return (raw.toDouble() * xAxisFactor) + xAxisOffset
    }

    fun rawYToPhysical(raw: Number): Double {
        return (raw.toDouble() * yAxisFactor) + yAxisOffset
    }
}
