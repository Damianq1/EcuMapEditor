package com.winols.app.core

data class MapDefinition(
    val id: String = "",
    val rows: Int = 1,
    val cols: Int = 1
)

class MapFinderEngine {
    fun findMaps() {
        // Uzupełniono brakujące parametry 'id', 'rows', 'cols'
        val map = MapDefinition(id = "default_map", rows = 16, cols = 16)
    }
}
