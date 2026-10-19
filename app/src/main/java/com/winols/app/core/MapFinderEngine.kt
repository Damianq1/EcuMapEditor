package com.winols.app.core

class MapFinderEngine {
    fun findMaps(): List<MapDefinition> {
        // Przykład poprawnego wywołania konstruktora z wymaganymi parametrami
        return listOf(
            MapDefinition(
                name = "Example Map",
                address = 0x4000,
                dataType = "UINT16",
                endianness = "BIG"
            )
        )
    }
}
