@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.jsonIgnoreUnknown
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

sealed interface BSLights : BSCustomData, BSVersioned {
    fun eventCount(): Int

    companion object {
        fun parse(element: JsonElement): BSLights =
            jsonIgnoreUnknown.decodeFromJsonElement<BSLightingV4>(element)
    }
}
