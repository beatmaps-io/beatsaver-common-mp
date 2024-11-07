@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.or
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject

@Serializable
data class BSDifficultyV3(
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val bpmEvents: OptionalProperty<List<OptionalProperty<BSBpmChange?>>?> = OptionalProperty.NotPresent,
    val rotationEvents: OptionalProperty<List<OptionalProperty<BSRotationEvent?>>?> = OptionalProperty.NotPresent,
    val colorNotes: OptionalProperty<List<OptionalProperty<BSNoteV3?>>?> = OptionalProperty.NotPresent,
    val bombNotes: OptionalProperty<List<OptionalProperty<BSBomb?>>?> = OptionalProperty.NotPresent,
    val obstacles: OptionalProperty<List<OptionalProperty<BSObstacleV3?>>?> = OptionalProperty.NotPresent,
    val sliders: OptionalProperty<List<OptionalProperty<BSSlider?>>?> = OptionalProperty.NotPresent,
    val burstSliders: OptionalProperty<List<OptionalProperty<BSBurstSlider?>>?> = OptionalProperty.NotPresent,
    val waypoints: OptionalProperty<List<OptionalProperty<BSWaypoint?>>?> = OptionalProperty.NotPresent,
    val basicBeatmapEvents: OptionalProperty<List<OptionalProperty<BSEventV3?>>?> = OptionalProperty.NotPresent,
    val colorBoostBeatmapEvents: OptionalProperty<List<OptionalProperty<BSBoostEvent?>>?> = OptionalProperty.NotPresent,
    val lightColorEventBoxGroups: OptionalProperty<List<OptionalProperty<BSLightColorEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightRotationEventBoxGroups: OptionalProperty<List<OptionalProperty<BSLightRotationEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val lightTranslationEventBoxGroups: OptionalProperty<List<OptionalProperty<BSLightTranslationEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val vfxEventBoxGroups: OptionalProperty<List<OptionalProperty<BSVfxEventBoxGroup?>>?> = OptionalProperty.NotPresent,
    val _fxEventsCollection: OptionalProperty<BSFxEventsCollection?> = OptionalProperty.NotPresent,
    val basicEventTypesWithKeywords: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent,
    val useNormalEventsAsCompatibleEvents: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,
    @SerialName("_customData") @ValidationName("_customData")
    override val customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSDiff, BSLights {
    override fun noteCount() = colorNotes.orEmpty().size
    override fun bombCount() = bombNotes.orEmpty().size
    override fun arcCount() = sliders.orEmpty().size
    override fun chainCount() = burstSliders.orEmpty().size

    override fun eventCount() = basicBeatmapEvents.orEmpty().size
    override fun obstacleCount() = obstacles.orEmpty().size
    private val firstAndLastLazy by lazy {
        colorNotes.orEmpty().sortedBy { note -> note.time }.let { sorted ->
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
        sli.withBpmEvents(bpmEvents.orEmpty()).let {
            it.timeToSeconds(firstAndLastLazy.second) - it.timeToSeconds(firstAndLastLazy.first)
        }.let { len ->
            if (len == 0f) 0f else noteCount() / len
        }
}

@Serializable
data class BSObstacleV3(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val duration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("w")
    val width: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("h")
    val height: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSBpmChange(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("m")
    val bpm: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSBoostEvent(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("o")
    val boost: OptionalProperty<Boolean?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

interface IBSEventBoxGroup<T : GroupableEventBox> {
    val beat: OptionalProperty<Float?>
    val groupId: OptionalProperty<Int?>
    val eventBoxes: OptionalProperty<List<OptionalProperty<T?>>?>
}

@Serializable
data class BSEventBoxGroup<T : GroupableEventBox>(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("g")
    override val groupId: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val eventBoxes: OptionalProperty<List<OptionalProperty<T?>>?> = OptionalProperty.NotPresent
) : IBSEventBoxGroup<T>, IBSObject by BSObject(beat) {
    constructor(beat: Float, groupId: Int, eventBoxes: List<T>) :
        this(OptionalProperty.Present(beat), OptionalProperty.Present(groupId), OptionalProperty.Present(eventBoxes.map { OptionalProperty.Present(it) }))
}

typealias BSLightColorEventBoxGroup = BSEventBoxGroup<BSLightColorEventBox>
typealias BSLightRotationEventBoxGroup = BSEventBoxGroup<BSLightRotationEventBox>
typealias BSLightTranslationEventBoxGroup = BSEventBoxGroup<BSLightTranslationEventBox>

@Serializable
data class BSVfxEventBoxGroup(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("g")
    override val groupId: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    override val eventBoxes: OptionalProperty<List<OptionalProperty<BSVfxEventBox?>>?> = OptionalProperty.NotPresent
) : IBSEventBoxGroup<BSVfxEventBox>

interface EventBox : BSIndexable {
    val beatDistributionParam: OptionalProperty<Float?>
    val beatDistributionParamType: OptionalProperty<Int?>
}

interface GroupableEventBox : EventBox {
    val indexFilter: OptionalProperty<BSIndexFilter?>
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
    val vfxBaseDataList: OptionalProperty<List<OptionalProperty<Int?>>?> = OptionalProperty.NotPresent
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
    val lightColorBaseDataList: OptionalProperty<List<OptionalProperty<BSLightColorBaseData?>>?> = OptionalProperty.NotPresent
) : GroupableEventBox

@Serializable
data class BSLightColorBaseData(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val transitionType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val colorType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val brightness: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val strobeFrequency: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

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
    val lightRotationBaseDataList: OptionalProperty<List<OptionalProperty<LightRotationBaseData?>>?> = OptionalProperty.NotPresent
) : GroupableEventBox

@Serializable
data class LightRotationBaseData(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
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
) : IBSObject by BSObject(beat)

@Serializable
data class BSLightTranslationEventBox(
    @SerialName("f")
    override val indexFilter: OptionalProperty<BSIndexFilter?> = OptionalProperty.NotPresent,
    @SerialName("w")
    override val beatDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("d")
    override val beatDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,

    @SerialName("s")
    val gapDistributionParam: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val gapDistributionParamType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val axis: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val flipTranslation: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("b")
    val gapDistributionShouldAffectFirstBaseEvent: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val gapDistributionEaseType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("l")
    val lightTranslationBaseDataList: OptionalProperty<List<OptionalProperty<LightTranslationBaseData?>>?> = OptionalProperty.NotPresent
) : GroupableEventBox {
    constructor(
        indexFilter: BSIndexFilter,
        beatDistributionParam: Float,
        beatDistributionParamType: Int,
        gapDistributionParam: Float,
        gapDistributionParamType: Int,
        axis: Int,
        flipTranslation: Int,
        gapDistributionShouldAffectFirstBaseEvent: Int,
        gapDistributionEaseType: Int,
        lightTranslationBaseDataList: List<LightTranslationBaseData>
    ) :
        this(
            OptionalProperty.Present(indexFilter), OptionalProperty.Present(beatDistributionParam), OptionalProperty.Present(beatDistributionParamType),
            OptionalProperty.Present(gapDistributionParam), OptionalProperty.Present(gapDistributionParamType), OptionalProperty.Present(axis),
            OptionalProperty.Present(flipTranslation), OptionalProperty.Present(gapDistributionShouldAffectFirstBaseEvent), OptionalProperty.Present(gapDistributionEaseType),
            OptionalProperty.Present(lightTranslationBaseDataList.map { OptionalProperty.Present(it) })
        )
}

@Serializable
data class LightTranslationBaseData(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val usePreviousEventTranslationValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("t")
    val translation: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

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
) {
    constructor(type: Int, param0: Int, param1: Int, reversed: Int, chunks: Int, randomType: Int, seed: Int, limit: Float, alsoAffectsType: Int) :
        this(
            OptionalProperty.Present(type), OptionalProperty.Present(param0), OptionalProperty.Present(param1), OptionalProperty.Present(reversed),
            OptionalProperty.Present(chunks), OptionalProperty.Present(randomType), OptionalProperty.Present(seed), OptionalProperty.Present(limit),
            OptionalProperty.Present(alsoAffectsType)
        )
}

@Serializable
data class BSWaypoint(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val offsetDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSBomb(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSNoteV3(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val angleOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val color: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSBurstSlider(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
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
) : IBSObject by BSObject(beat) {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSSlider(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
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
) : IBSObject by BSObject(beat) {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSEventV3(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("et")
    val eventType: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val value: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("f")
    val floatValue: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSRotationEvent(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val executionTime: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotation: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBSObject by BSObject(beat)

@Serializable
data class BSFxEventsCollection(
    @SerialName("_il")
    val intEventsList: OptionalProperty<List<OptionalProperty<BSIntFxEventBaseData?>>?> = OptionalProperty.NotPresent,
    @SerialName("_fl")
    val floatEventsList: OptionalProperty<List<OptionalProperty<BSFloatFxEventBaseData?>>?> = OptionalProperty.NotPresent
)

abstract class BSFxEventBaseData<T> : IBSObject {
    abstract val usePreviousEventValue: OptionalProperty<Int?>
    abstract val value: OptionalProperty<T?>
}

@Serializable
data class BSIntFxEventBaseData(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    override val usePreviousEventValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("v")
    override val value: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSFxEventBaseData<Int>(), IBSObject by BSObject(beat)

@Serializable
data class BSFloatFxEventBaseData(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    override val usePreviousEventValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("v")
    override val value: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val easeType: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSFxEventBaseData<Float>(), IBSObject by BSObject(beat)
