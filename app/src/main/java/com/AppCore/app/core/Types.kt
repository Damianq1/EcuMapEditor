package com.AppCore.app.core

// Przeniesienie typealias na poziom pliku (top-level), aby uniknąć błędu "Nested and local type aliases are not supported"
// Popraw te definicje zgodnie z faktycznymi typami używanymi w projekcie:
typealias MapData = Map<String, Any>
typealias MapList = List<MapData>
typealias Callback = (Boolean) -> Unit
