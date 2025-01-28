@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.map

import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.beatsaber.custom.BSCustomData
import io.beatmaps.common.beatsaber.custom.BSMapCustomData
import io.beatmaps.common.jsonIgnoreUnknown
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

sealed interface BSLights : BSCustomData<BSMapCustomData>, BSVersioned {
    fun eventCount(): Int

    companion object {
        fun parse(element: JsonElement) =
            element.jsonObject.parseBS({ version ->
                when (version.major) {
                    4 -> jsonIgnoreUnknown.decodeFromJsonElement<BSLightingV4>(element)
                    else -> jsonIgnoreUnknown.decodeFromJsonElement<BSDifficultyV3>(element)
                }
            }) {
                jsonIgnoreUnknown.decodeFromJsonElement<BSDifficulty>(element)
            }
    }
}
