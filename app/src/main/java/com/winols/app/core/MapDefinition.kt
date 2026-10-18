package com.winols.app.core

enum class DataType {
    UINT8, INT8, UINT16, INT16, UINT32, INT32, FLOAT32
}

data class MapDefinition(
    val id: String,
    val name: String,
    val address: Int,
    val rows: Int,
    val cols: Int,
    val dataType: DataType,
    val factor: Double = 1.0,
    val offset: Double = 0.0
)
