package io.beatmaps.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val json = Json {
    serializersModule = SerializersModule {
        modlog()
    }
    prettyPrint = true
}