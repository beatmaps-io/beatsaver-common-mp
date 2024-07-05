@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KProperty1

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
    val eventBoxGroups: OptionalProperty<List<OptionalProperty<BSEventBoxGroupV4?>>?> = OptionalProperty.NotPresent,
    val indexFilters: OptionalProperty<List<OptionalProperty<BSIndexFilterV4?>>?> = OptionalProperty.NotPresent,
    val lightColorEventBoxes: OptionalProperty<List<OptionalProperty<BSLightColorEventBoxV4?>>?> = OptionalProperty.NotPresent,
    val lightColorEvents: OptionalProperty<List<OptionalProperty<BSLightColorEventV4?>>?> = OptionalProperty.NotPresent,
    val lightRotationEventBoxes: OptionalProperty<List<OptionalProperty<BSLightRotationEventBoxV4?>>?> = OptionalProperty.NotPresent,
    val lightRotationEvents: OptionalProperty<List<OptionalProperty<BSLightRotationEventV4?>>?> = OptionalProperty.NotPresent,
    val lightTranslationEventBoxes: OptionalProperty<List<OptionalProperty<BSLightTranslationEventBoxV4?>>?> = OptionalProperty.NotPresent,
    val lightTranslationEvents: OptionalProperty<List<OptionalProperty<BSLightTranslationEventV4?>>?> = OptionalProperty.NotPresent,
    val fxEventBoxes: OptionalProperty<List<OptionalProperty<BSFxEventBoxV4?>>?> = OptionalProperty.NotPresent,
    val floatFxEvents: OptionalProperty<List<OptionalProperty<BSFxEventV4?>>?> = OptionalProperty.NotPresent,
    val useNormalEventsAsCompatibleEvents: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,
    override val customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSLights {
    override fun eventCount() = basicEvents.orEmpty().size +
        colorBoostEvents.orEmpty().size + eventBoxGroups.orEmpty().sumOf { it.eventBoxes.orEmpty().size }
}

typealias BSIndexedLightV4<T> = BSIndexedGeneric<BSLightingV4, T>

interface BoxedEvent {
    val transitionType: OptionalProperty<Int?>
    val easeType: OptionalProperty<Int?>
    val magnitude: OptionalProperty<Float?>
}
interface EventBoxV4 : EventBox {
    val magnitudeDistributionParam: OptionalProperty<Float?>
    val magnitudeDistributionParamType: OptionalProperty<Int?>
    val distributionShouldAffectFirstBaseEvent: OptionalProperty<Int?>
    val easeType: OptionalProperty<Int?>
}

enum class EventBoxType(
    val id: Int,
    val boxField: KProperty1<BSLightingV4, OptionalProperty<List<OptionalProperty<EventBox?>>?>>? = null,
    val field: KProperty1<BSLightingV4, OptionalProperty<List<OptionalProperty<BoxedEvent?>>?>>? = null
) {
    None(0),
    Color(1, BSLightingV4::lightColorEventBoxes, BSLightingV4::lightColorEvents),
    Rotation(2, BSLightingV4::lightRotationEventBoxes, BSLightingV4::lightRotationEvents),
    Translation(3, BSLightingV4::lightTranslationEventBoxes, BSLightingV4::lightTranslationEvents),
    FX(4, BSLightingV4::fxEventBoxes, BSLightingV4::floatFxEvents)
}

@Serializable
data class BSEventV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedLightV4<BSEventDataV4>(), IBSObject by BSObject(beat) {
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
) : BSIndexedLightV4<BSBoostEventDataV4>(), IBSObject by BSObject(beat) {
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
) : BSIndexedLightV4<BSWaypointDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSLightingV4::waypointsData
}

@Serializable
data class BSWaypointDataV4(
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val offsetDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSEventBoxGroupV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("g")
    val groupId: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val eventBoxes: OptionalProperty<List<OptionalProperty<BSEventBoxV4?>>?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat) {
    fun enumValue() =
        type.orNull()?.let {
            EventBoxType.entries[it]
        }
}

@Serializable
data class BSEventBoxV4(
    @SerialName("f")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val eventBoxIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val events: OptionalProperty<List<OptionalProperty<BSEventBoxDataV4?>>?> = OptionalProperty.NotPresent
) : BSIndexedLightV4<BSIndexFilterV4>() {
    override val prop = BSLightingV4::indexFilters
    fun getEventBox(diff: BSLightingV4, type: EventBoxType) =
        type.boxField?.get(diff)?.let {
            getForProp(eventBoxIndex, it)
        }
}

@Serializable
data class BSEventBoxDataV4(
    @SerialName("b")
    val beatOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexed {
    fun getEvent(diff: BSLightingV4, type: EventBoxType) =
        type.field?.get(diff)?.let { list ->
            index.orNull()?.let {
                list.orEmpty().getOrNull(it)
            }
        }
}

@Serializable
data class BSIndexFilterV4(
    @SerialName("c")
    val chunks: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val param0: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val param1: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val reversed: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("n")
    val randomType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val seed: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val limit: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val alsoAffectsType: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSLightColorEventBoxV4(
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    override val magnitudeDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val magnitudeDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    override val distributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, EventBoxV4

@Serializable
data class BSLightColorEventV4(
    @SerialName("p")
    override val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val colorType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    override val magnitude: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val strobeFrequency: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("sb")
    val strobeBrightness: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("sf")
    val strobeFade: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, BoxedEvent

@Serializable
data class BSLightRotationEventBoxV4(
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    override val magnitudeDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val magnitudeDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    override val distributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val invertAxis: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, EventBoxV4

@Serializable
data class BSLightRotationEventV4(
    @SerialName("p")
    override val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    override val magnitude: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val loopCount: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, BoxedEvent

@Serializable
data class BSLightTranslationEventBoxV4(
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    override val magnitudeDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val magnitudeDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    override val distributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val axis: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val invertAxis: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, EventBoxV4

@Serializable
data class BSLightTranslationEventV4(
    @SerialName("p")
    override val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val magnitude: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSIndexable, BoxedEvent

@Serializable
data class BSFxEventBoxV4(
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    override val magnitudeDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    override val magnitudeDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    override val distributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable, EventBoxV4

@Serializable
data class BSFxEventV4(
    @SerialName("p")
    override val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("v")
    override val magnitude: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSIndexable, BoxedEvent
