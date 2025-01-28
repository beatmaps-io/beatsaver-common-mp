@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.custom

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.beatsaber.map.BSObject
import io.beatmaps.common.beatsaber.map.IBSObject
import io.beatmaps.common.beatsaber.map.ValidationName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement

interface CustomJsonEvent : IBSObject {
    val type: String
    val data: JsonElement
}

@Serializable
data class CustomJsonEventV2(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    override val type: String,
    override val data: JsonElement
) : CustomJsonEvent, IBSObject by BSObject(beat)

@Serializable
data class CustomJsonEventV3(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val type: String,
    @SerialName("d")
    override val data: JsonElement
) : CustomJsonEvent, IBSObject by BSObject(beat)
