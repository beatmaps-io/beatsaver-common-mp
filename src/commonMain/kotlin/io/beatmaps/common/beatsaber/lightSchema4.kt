@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject

@Serializable
data class BSLightingV4(
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val basicEvents: OptionalProperty<List<OptionalProperty<BSEventV4?>>?> = OptionalProperty.NotPresent,
    val basicEventsData: OptionalProperty<List<OptionalProperty<BSEventDataV4?>>?> = OptionalProperty.NotPresent,
    val colorBoostEvents: OptionalProperty<List<OptionalProperty<BSBoostEventV4?>>?> = OptionalProperty.NotPresent,
    val colorBoostEventsData: OptionalProperty<List<OptionalProperty<BSBoostEventDataV4?>>?> = OptionalProperty.NotPresent,
    val waypoints: OptionalProperty<List<OptionalProperty<BSWaypointV4?>>?> = OptionalProperty.NotPresent,
    val waypointsData: OptionalProperty<List<OptionalProperty<BSWaypointDataV4?>>?> = OptionalProperty.NotPresent,
    val basicEventTypesWithKeywords: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent,

    /*val eventBoxGroups: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val indexFilters: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightColorEventBoxes: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightColorEvents: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightRotationEventBoxes: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightRotationEvents: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightTranslationEventBoxes: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightTranslationEvents: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val fxEventBoxes: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val floatFxEvents: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val useNormalEventsAsCompatibleEvents: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,*/
    override val customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSLights {
    override fun eventCount() = basicEvents.orEmpty().size
}

typealias BSIndexedLightV4<T> = BSIndexedGeneric<BSLightingV4, T>

@Serializable
data class BSEventV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedLightV4<BSEventDataV4>() {
    override val prop = BSLightingV4::basicEventsData
}

@Serializable
data class BSEventDataV4(
    @SerialName("t")
    val eventType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val value: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val floatValue: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSBoostEventV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedLightV4<BSBoostEventDataV4>() {
    override val prop = BSLightingV4::colorBoostEventsData
}

@Serializable
data class BSBoostEventDataV4(
    @SerialName("b")
    val boost: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSWaypointV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedLightV4<BSWaypointDataV4>() {
    override val prop = BSLightingV4::waypointsData
}

@Serializable
data class BSWaypointDataV4(
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val offsetDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable
