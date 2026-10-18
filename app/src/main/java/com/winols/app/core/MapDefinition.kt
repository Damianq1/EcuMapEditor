package com.winols.app.core

data class MapDefinition(
    val id: String,
    val name: String,
    val address: Int,
    val rows: Int,
    val cols: Int,
    val dataType: DataType,
    val endianness: Endianness
)
