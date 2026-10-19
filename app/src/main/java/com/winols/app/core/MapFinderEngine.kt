package com.winols.app.core

// Usunięto duplikat deklaracji MapDefinition (powinien być zdefiniowany tylko w jednym miejscu, np. w pliku modelu)

class MapFinderEngine {
    fun parseMap(data: ByteArray): MapDefinition {
        // Poprawiono parametry wywołania konstruktora MapDefinition zgodnie z jego właściwą definicją
        return MapDefinition(
            name = "Detected Map",
            address = 0,
            dataType = DataType.UINT16,
            endianness = Endianness.LITTLE_ENDIAN
        )
    }
}
