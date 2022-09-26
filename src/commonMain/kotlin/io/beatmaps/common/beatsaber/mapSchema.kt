package io.beatmaps.common.beatsaber

import SongLengthInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.properties.ReadOnlyProperty

interface BSCustomData {
    val _customData: Any?

    fun getCustomData() = when (_customData) {
        is Map<*, *> -> _customData
        else -> mapOf<String, String>()
    } as Map<*, *>
}

fun <T : BSCustomData> List<T>.withoutFake() = this.filter { obj -> (obj.getCustomData()["_fake"] as? JsonPrimitive)?.booleanOrNull != true }

@Serializable
data class BSDifficulty(
    @SerialName("_version")
    override val version: String? = null,
    val _notes: List<BSNote> = listOf(),
    val _obstacles: List<BSObstacle> = listOf(),
    val _events: List<BSEvent> = listOf(),
    val _waypoints: JsonArray? = null,
    val _specialEventsKeywordFilters: JsonObject? = null,
    override val _customData: JsonObject? = null,
    val _BPMChanges: JsonArray? = null
) : BSDiff {
    private val noteCountLazy by lazy { _notes.withoutFake().partition { note -> note._type != 3 } }
    override fun noteCount() = noteCountLazy.first.size
    override fun bombCount() = noteCountLazy.second.size
    override fun arcCount() = 0
    override fun chainCount() = 0
    override fun obstacleCount() = _obstacles.withoutFake().size
    override fun eventCount() = _events.size
    private val songLengthLazy by lazy {
        _notes.sortedBy { note -> note.time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.last().time - sorted.first().time
            } else 0f
        }
    }
    override fun songLength() = songLengthLazy

    private val maxScorePerBlock = 115
    override fun maxScore() =
        noteCount().let { n ->
            (if (n > (1 + 4 + 8)) maxScorePerBlock * 8 * (n - 13) else 0) +
                (if (n > (1 + 4)) maxScorePerBlock * 4 * (n.coerceAtMost(13) - 5) else 0) +
                (if (n > 1) maxScorePerBlock * 2 * (n.coerceAtMost(5) - 1) else 0) +
                n.coerceAtMost(1) * maxScorePerBlock
        }

    override fun mappedNps(sli: SongLengthInfo) =
        sli.timeToSeconds(songLength()).let { len ->
            if (len == 0f) 0f else noteCount() / len
        }
}

fun <T> orNegativeInfinity(block: (T) -> Float?): ReadOnlyProperty<T, Float> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef) ?: Float.NEGATIVE_INFINITY }
fun <T> orMinValue(block: (T) -> Int?): ReadOnlyProperty<T, Int> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef) ?: Int.MIN_VALUE }

abstract class BSObject {
    abstract val _time: Float?
    val time by orNegativeInfinity { _time }
}

@Serializable
data class BSNote(
    override val _time: Float? = null,
    val _lineIndex: Int? = null,
    val _lineLayer: Int? = null,
    val _type: Int? = null,
    val _cutDirection: Int? = null,
    override val _customData: JsonObject? = null
) : BSCustomData, BSObject() {
    val lineIndex by orMinValue { _lineIndex }
    val lineLayer by orMinValue { _lineLayer }
    val type by orMinValue { _type }
    val cutDirection by orMinValue { _cutDirection }
}

@Serializable
data class BSObstacle(
    override val _time: Float? = null,
    val _lineIndex: Int? = null,
    val _type: Int? = null,
    val _duration: Float? = null,
    val _width: Int? = null,
    override val _customData: JsonObject? = null
) : BSCustomData, BSObject()

@Serializable
data class BSEvent(
    override val _time: Float? = null,
    val _type: Int? = null,
    val _value: Int? = null,
    override val _customData: JsonObject? = null
) : BSCustomData, BSObject()

sealed interface BSDiff : BSCustomData {
    val version: String?
    fun noteCount(): Int
    fun bombCount(): Int
    fun arcCount(): Int
    fun chainCount(): Int
    fun eventCount(): Int
    fun obstacleCount(): Int
    fun songLength(): Float
    fun maxScore(): Int
    fun mappedNps(sli: SongLengthInfo): Float
}

@Serializable
data class BSDifficultyV3(
    override val version: String? = null,
    val bpmEvents: List<BSBpmChange>,
    val rotationEvents: List<BSRotationEvent>,
    val colorNotes: List<BSNoteV3>,
    val bombNotes: List<BSBomb>,
    val obstacles: List<BSObstacleV3>,
    val sliders: List<BSSlider>,
    val burstSliders: List<BSBurstSlider>,
    val waypoints: List<BSWaypoint>,
    val basicBeatmapEvents: List<BSEventV3>,
    val colorBoostBeatmapEvents: List<BSBoostEvent>,
    val lightColorEventBoxGroups: List<BSLightColorEventBoxGroup>,
    val lightRotationEventBoxGroups: List<BSLightRotationEventBoxGroup>,
    val basicEventTypesWithKeywords: JsonObject? = null,
    val useNormalEventsAsCompatibleEvents: Boolean? = null,

    override val _customData: JsonObject? = null
) : BSDiff {
    override fun noteCount() = colorNotes.size
    override fun bombCount() = bombNotes.size
    override fun arcCount() = sliders.size
    override fun chainCount() = burstSliders.size

    override fun eventCount() = basicBeatmapEvents.size
    override fun obstacleCount() = obstacles.size
    private val firstAndLastLazy by lazy {
        colorNotes.sortedBy { note -> note.time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.first().time to sorted.last().time
            } else 0f to 0f
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
    val beat: Float? = null,
    val x: Int? = null,
    val y: Int? = null,
    @SerialName("d")
    val duration: Float? = null,
    @SerialName("w")
    val width: Int? = null,
    @SerialName("h")
    val height: Int? = null
)

@Serializable
data class BSBpmChange(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("m")
    val bpm: Float? = null
)

@Serializable
data class BSBoostEvent(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("o")
    val boost: Boolean? = null
)

typealias BSLightColorEventBoxGroup = BSEventBoxGroup<BSLightColorEventBox>
typealias BSLightRotationEventBoxGroup = BSEventBoxGroup<BSLightRotationEventBox>

@Serializable
data class BSEventBoxGroup<T : GroupableEventBox>(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("g")
    val groupId: Int? = null,
    @SerialName("e")
    val eventBoxes: List<T>? = null
)

interface GroupableEventBox {
    val indexFilter: BSIndexFilter?
    val beatDistributionParam: Float?
    val beatDistributionParamType: Int?
}

@Serializable
data class BSLightColorEventBox(
    @SerialName("f")
    override val indexFilter: BSIndexFilter? = null,
    @SerialName("w")
    override val beatDistributionParam: Float? = null,
    @SerialName("d")
    override val beatDistributionParamType: Int? = null,

    @SerialName("r")
    val brightnessDistributionParam: Float? = null,
    @SerialName("t")
    val brightnessDistributionParamType: Int? = null,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: Int? = null,
    @SerialName("e")
    val lightColorBaseDataList: List<BSLightColorBaseData>? = null
) : GroupableEventBox

@Serializable
data class BSLightColorBaseData(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("i")
    val transitionType: Int? = null,
    @SerialName("c")
    val colorType: Int? = null,
    @SerialName("s")
    val brightness: Float? = null,
    @SerialName("f")
    val strobeFrequency: Int? = null
)

@Serializable
data class BSLightRotationEventBox(
    @SerialName("f")
    override val indexFilter: BSIndexFilter? = null,
    @SerialName("w")
    override val beatDistributionParam: Float? = null,
    @SerialName("d")
    override val beatDistributionParamType: Int? = null,

    @SerialName("s")
    val rotationDistributionParam: Float? = null,
    @SerialName("t")
    val rotationDistributionParamType: Int? = null,
    @SerialName("a")
    val axis: Int? = null,
    @SerialName("r")
    val flipRotation: Int? = null,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: Int? = null,
    @SerialName("l")
    val lightRotationBaseDataList: List<LightRotationBaseData>? = null
) : GroupableEventBox

@Serializable
data class LightRotationBaseData(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("p")
    val usePreviousEventRotationValue: Int? = null,
    @SerialName("e")
    val easeType: Int? = null,
    @SerialName("l")
    val loopsCount: Int? = null,
    @SerialName("r")
    val rotation: Float? = null,
    @SerialName("o")
    val rotationDirection: Int? = null
)

@Serializable
data class BSIndexFilter(
    @SerialName("f")
    val type: Int? = null,
    @SerialName("p")
    val param0: Int? = null,
    @SerialName("t")
    val param1: Int? = null,
    @SerialName("r")
    val reversed: Int? = null
)

@Serializable
data class BSWaypoint(
    @SerialName("b")
    val beat: Float? = null,
    val x: Int? = null,
    val y: Int? = null,
    @SerialName("d")
    val offsetDirection: Int? = null
)

@Serializable
data class BSBomb(
    @SerialName("b")
    val beat: Float? = null,
    val x: Int? = null,
    val y: Int? = null
)

@Serializable
data class BSNoteV3(
    @SerialName("b")
    override val _time: Float? = null,
    val x: Int? = null,
    val y: Int? = null,
    @SerialName("a")
    val angleOffset: Int? = null,
    @SerialName("c")
    val color: Int? = null,
    @SerialName("d")
    val direction: Int? = null
) : BSObject()

@Serializable
data class BSBurstSlider(
    @SerialName("b")
    override val _time: Float? = null,
    @SerialName("c")
    val color: Int? = null,
    val x: Int? = null,
    val y: Int? = null,
    @SerialName("d")
    val direction: Int? = null,
    @SerialName("tb")
    val tailBeat: Float? = null,
    @SerialName("tx")
    val tailX: Int? = null,
    @SerialName("ty")
    val tailY: Int? = null,
    @SerialName("sc")
    val sliceCount: Int? = null,
    @SerialName("s")
    val squishAmount: Float? = null
) : BSObject() {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSSlider(
    @SerialName("b")
    override val _time: Float? = null,
    @SerialName("c")
    val color: Int? = null,
    val x: Int? = null,
    val y: Int? = null,
    @SerialName("d")
    val direction: Int? = null,
    @SerialName("tb")
    val tailBeat: Float? = null,
    @SerialName("tx")
    val tailX: Int? = null,
    @SerialName("ty")
    val tailY: Int? = null,
    @SerialName("mu")
    val headControlPointLengthMultiplier: Float? = null,
    @SerialName("tmu")
    val tailControlPointLengthMultiplier: Float? = null,
    @SerialName("tc")
    val tailCutDirection: Int? = null,
    @SerialName("m")
    val sliderMidAnchorMode: Int? = null
) : BSObject() {
    val tailTime by orNegativeInfinity { tailBeat }
}

@Serializable
data class BSEventV3(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("et")
    val eventType: Int? = null,
    @SerialName("i")
    val value: Int? = null,
    @SerialName("f")
    val floatValue: Float? = null
)

@Serializable
data class BSRotationEvent(
    @SerialName("b")
    val beat: Float? = null,
    @SerialName("e")
    val executionTime: Int? = null,
    @SerialName("r")
    val rotation: Float? = null
)
