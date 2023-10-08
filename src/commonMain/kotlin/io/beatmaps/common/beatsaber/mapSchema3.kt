@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject

@Serializable
data class BSDifficultyV3(
    override val version: String? = null,
    val bpmEvents: OptionalProperty<List<BSBpmChange>?> = OptionalProperty.NotPresent,
    val rotationEvents: OptionalProperty<List<BSRotationEvent>?> = OptionalProperty.NotPresent,
    val colorNotes: OptionalProperty<List<BSNoteV3>> = OptionalProperty.NotPresent,
    val bombNotes: OptionalProperty<List<BSBomb>> = OptionalProperty.NotPresent,
    val obstacles: OptionalProperty<List<BSObstacleV3>> = OptionalProperty.NotPresent,
    val sliders: OptionalProperty<List<BSSlider>> = OptionalProperty.NotPresent,
    val burstSliders: OptionalProperty<List<BSBurstSlider>> = OptionalProperty.NotPresent,
    val waypoints: OptionalProperty<List<BSWaypoint>> = OptionalProperty.NotPresent,
    val basicBeatmapEvents: OptionalProperty<List<BSEventV3>> = OptionalProperty.NotPresent,
    val colorBoostBeatmapEvents: OptionalProperty<List<BSBoostEvent>> = OptionalProperty.NotPresent,
    val lightColorEventBoxGroups: OptionalProperty<List<BSLightColorEventBoxGroup>> = OptionalProperty.NotPresent,
    val lightRotationEventBoxGroups: OptionalProperty<List<BSLightRotationEventBoxGroup>> = OptionalProperty.NotPresent,
    val basicEventTypesWithKeywords: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent,
    val vfxEventBoxGroups: OptionalProperty<List<BSVfxEventBoxGroup>> = OptionalProperty.NotPresent,
    val _fxEventsCollection: OptionalProperty<BSFxEventsCollection> = OptionalProperty.NotPresent,
    val useNormalEventsAsCompatibleEvents: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,

    override val _customData: JsonObject? = null
) : BSDiff {
    override fun noteCount() = colorNotes.orNull()?.size ?: 0
    override fun bombCount() = bombNotes.orNull()?.size ?: 0
    override fun arcCount() = sliders.orNull()?.size ?: 0
    override fun chainCount() = burstSliders.orNull()?.size ?: 0

    override fun eventCount() = basicBeatmapEvents.orNull()?.size ?: 0
    override fun obstacleCount() = obstacles.orNull()?.size ?: 0
    private val firstAndLastLazy by lazy {
        (colorNotes.orNull() ?: listOf()).sortedBy { note -> note.time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.first().time to sorted.last().time
            } else {
                0f to 0f
            }
        }
    }
    override fun songLength() = firstAndLastLazy.second - firstAndLastLazy.first

    private val maxScoreLazy by lazy { computeMaxMultipliedScoreForBeatmap(this) }
    override fun maxScore() = maxScoreLazy
    override fun mappedNps(sli: SongLengthInfo) =
        sli.let {
            it.timeToSeconds(firstAndLastLazy.second) - it.timeToSeconds(firstAndLastLazy.first)
        }.let { len ->
            if (len == 0f) 0f else noteCount() / len
        }
}

@Serializable
data class BSObstacleV3(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val duration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("w")
    val width: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("h")
    val height: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSBpmChange(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("m")
    val bpm: OptionalProperty<Float?> = OptionalProperty.NotPresent
)

@Serializable
data class BSBoostEvent(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("o")
    val boost: OptionalProperty<Boolean?> = OptionalProperty.NotPresent
)

typealias BSVfxEventBoxGroup = BSEventBoxGroup<BSVfxEventBox>
typealias BSLightColorEventBoxGroup = BSEventBoxGroup<BSLightColorEventBox>
typealias BSLightRotationEventBoxGroup = BSEventBoxGroup<BSLightRotationEventBox>

@Serializable
data class BSEventBoxGroup<T : GroupableEventBox>(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("g")
    val groupId: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val eventBoxes: OptionalProperty<List<T>?> = OptionalProperty.NotPresent
)

interface GroupableEventBox {
    val indexFilter: OptionalProperty<BSIndexFilter?>
    val beatDistributionParam: OptionalProperty<Float?>
    val beatDistributionParamType: OptionalProperty<Int?>
}

@Serializable
data class BSVfxEventBox(
    @SerialName("f")
    override val indexFilter: OptionalProperty<BSIndexFilter?> = OptionalProperty.NotPresent,
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,

    @SerialName("s")
    val vfxDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val vfxDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val vfxDistributionEaseType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    val vfxDistributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val vfxBaseDataList: OptionalProperty<List<Int>?> = OptionalProperty.NotPresent
) : GroupableEventBox

@Serializable
data class BSLightColorEventBox(
    @SerialName("f")
    override val indexFilter: OptionalProperty<BSIndexFilter?> = OptionalProperty.NotPresent,
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,

    @SerialName("r")
    val brightnessDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val brightnessDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val lightColorBaseDataList: OptionalProperty<List<BSLightColorBaseData>?> = OptionalProperty.NotPresent
) : GroupableEventBox

@Serializable
data class BSLightColorBaseData(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val colorType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val brightness: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val strobeFrequency: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSLightRotationEventBox(
    @SerialName("f")
    override val indexFilter: OptionalProperty<BSIndexFilter?> = OptionalProperty.NotPresent,
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,

    @SerialName("s")
    val rotationDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val rotationDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val axis: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val flipRotation: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val lightRotationBaseDataList: OptionalProperty<List<LightRotationBaseData>?> = OptionalProperty.NotPresent
) : GroupableEventBox

@Serializable
data class LightRotationBaseData(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val usePreviousEventRotationValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val loopsCount: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotation: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("o")
    val rotationDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSIndexFilter(
    @SerialName("f")
    val type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val param0: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val param1: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val reversed: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val chunks: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("n")
    val randomType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val seed: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val limit: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val alsoAffectsType: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSWaypoint(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val offsetDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSBomb(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent
)

@Serializable
data class BSNoteV3(
    @SerialName("b")
    override val _time: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val angleOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val color: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSObject()

@Serializable
data class BSBurstSlider(
    @SerialName("b")
    override val _time: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val color: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("tb")
    val tailBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tx")
    val tailX: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ty")
    val tailY: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("sc")
    val sliceCount: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val squishAmount: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSObject() {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSSlider(
    @SerialName("b")
    override val _time: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val color: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("tb")
    val tailBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tx")
    val tailX: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ty")
    val tailY: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("mu")
    val headControlPointLengthMultiplier: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tmu")
    val tailControlPointLengthMultiplier: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tc")
    val tailCutDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("m")
    val sliderMidAnchorMode: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSObject() {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSEventV3(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("et")
    val eventType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val value: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val floatValue: OptionalProperty<Float?> = OptionalProperty.NotPresent
)

@Serializable
data class BSRotationEvent(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val executionTime: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotation: OptionalProperty<Float?> = OptionalProperty.NotPresent
)

@Serializable
data class BSFxEventsCollection(
    @SerialName("_il")
    val intEventsList: OptionalProperty<List<BSIntFxEventBaseData>?> = OptionalProperty.NotPresent,
    @SerialName("_fl")
    val floatEventsList: OptionalProperty<List<BSFloatFxEventBaseData>?> = OptionalProperty.NotPresent
)

@Serializable
data class BSFxEventBaseData<T>(
    @SerialName("b")
    val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val usePreviousEventValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("v")
    val value: OptionalProperty<T?> = OptionalProperty.NotPresent
)

typealias BSIntFxEventBaseData = BSFxEventBaseData<Int>
typealias BSFloatFxEventBaseData = BSFxEventBaseData<Float>
