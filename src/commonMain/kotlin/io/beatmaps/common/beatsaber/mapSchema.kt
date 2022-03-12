package io.beatmaps.common.beatsaber

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

interface BSCustomData {
    val _customData: Any?

    fun getCustomData() = when (_customData) {
        is Map<*, *> -> _customData
        else -> mapOf<String, String>()
    } as Map<*, *>
}

fun <T : BSCustomData> List<T>.withoutFake() = this.filter { obj -> obj.getCustomData()["_fake"] != true }

@Serializable
data class BSDifficulty(
    val _version: String?,
    val _notes: List<BSNote> = listOf(),
    val _obstacles: List<BSObstacle> = listOf(),
    val _events: List<BSEvent> = listOf(),
    val _waypoints: JsonArray? = null,
    val _specialEventsKeywordFilters: JsonObject?,
    override val _customData: JsonObject?,
    val _BPMChanges: JsonArray? = null
) : BSDiff {
    override fun noteCount() = _notes.withoutFake().filter { note -> note._type != 3 }.size
    override fun bombCount() = _notes.withoutFake().filter { note -> note._type == 3 }.size
    override fun obstacleCount() = _obstacles.withoutFake().size
    override fun eventCount() = _events.size
    override fun songLength() =
        _notes.sortedBy { note -> note._time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.last()._time - sorted.first()._time
            } else 0f
        }

    private val maxScorePerBlock = 115
    override fun maxScore() =
        noteCount().let { n ->
            (if (n > (1 + 4 + 8)) maxScorePerBlock * 8 * (n - 13) else 0) +
                (if (n > (1 + 4)) maxScorePerBlock * 4 * (n.coerceAtMost(13) - 5) else 0) +
                (if (n > 1) maxScorePerBlock * 2 * (n.coerceAtMost(5) - 1) else 0) +
                n.coerceAtMost(1) * maxScorePerBlock
        }
}

@Serializable
data class BSNote(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _lineLayer: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _cutDirection: Int = Int.MIN_VALUE,
    override val _customData: JsonObject?
) : BSCustomData

@Serializable
data class BSObstacle(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _duration: Long = Long.MIN_VALUE,
    val _width: Int = Int.MIN_VALUE,
    override val _customData: JsonObject?
) : BSCustomData

@Serializable
data class BSEvent(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _type: Int = Int.MIN_VALUE,
    val _value: Int = Int.MIN_VALUE,
    override val _customData: JsonObject?
) : BSCustomData

sealed interface BSDiff : BSCustomData {
    fun noteCount(): Int
    fun bombCount(): Int
    fun eventCount(): Int
    fun obstacleCount(): Int
    fun songLength(): Float
    fun maxScore(): Int
}

@Serializable
data class BSDifficultyV3(
    val version: String?,
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
    val basicEventTypesWithKeywords: JsonObject,
    val useNormalEventsAsCompatibleEvents: Boolean,

    override val _customData: JsonObject? = null
) : BSDiff {
    override fun noteCount() = colorNotes.size
    override fun bombCount() = bombNotes.size
    override fun eventCount() = basicBeatmapEvents.size
    override fun obstacleCount() = obstacles.size
    override fun songLength() = colorNotes.sortedBy { note -> note.beat }.let { sorted ->
        if (sorted.isNotEmpty()) {
            sorted.last().beat - sorted.first().beat
        } else 0f
    }

    override fun maxScore() = computeMaxMultipliedScoreForBeatmap(this)
}

@Serializable
data class BSObstacleV3(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE,
    @SerialName("d")
    val duration: Float = Float.NEGATIVE_INFINITY,
    @SerialName("w")
    val width: Int = Int.MIN_VALUE,
    @SerialName("h")
    val height: Int = Int.MIN_VALUE
)

@Serializable
data class BSBpmChange(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("m")
    val bpm: Float = Float.NEGATIVE_INFINITY
)

@Serializable
data class BSBoostEvent(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("o")
    val boost: Boolean = false
)

typealias BSLightColorEventBoxGroup = BSEventBoxGroup<BSLightColorEventBox>
typealias BSLightRotationEventBoxGroup = BSEventBoxGroup<BSLightRotationEventBox>

@Serializable
data class BSEventBoxGroup<T : GroupableEventBox>(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("g")
    val groupId: Int = Int.MIN_VALUE,
    @SerialName("e")
    val eventBoxes: List<T> = listOf()
)

interface GroupableEventBox {
    val indexFilter: BSIndexFilter?
    val beatDistributionParam: Float
    val beatDistributionParamType: Int
}

@Serializable
data class BSLightColorEventBox(
    @SerialName("f")
    override val indexFilter: BSIndexFilter? = null,
    @SerialName("w")
    override val beatDistributionParam: Float = Float.NEGATIVE_INFINITY,
    @SerialName("d")
    override val beatDistributionParamType: Int = Int.MIN_VALUE,

    @SerialName("r")
    val brightnessDistributionParam: Float = Float.NEGATIVE_INFINITY,
    @SerialName("t")
    val brightnessDistributionParamType: Int = Int.MIN_VALUE,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: Int = Int.MIN_VALUE,
    @SerialName("e")
    val lightColorBaseDataList: List<BSLightColorBaseData> = listOf()
) : GroupableEventBox

@Serializable
data class BSLightColorBaseData(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("i")
    val transitionType: Int = Int.MIN_VALUE,
    @SerialName("c")
    val colorType: Int = Int.MIN_VALUE,
    @SerialName("s")
    val brightness: Float = Float.NEGATIVE_INFINITY,
    @SerialName("f")
    val strobeFrequency: Int = Int.MIN_VALUE
)

@Serializable
data class BSLightRotationEventBox(
    @SerialName("f")
    override val indexFilter: BSIndexFilter? = null,
    @SerialName("w")
    override val beatDistributionParam: Float = Float.NEGATIVE_INFINITY,
    @SerialName("d")
    override val beatDistributionParamType: Int = Int.MIN_VALUE,

    @SerialName("s")
    val rotationDistributionParam: Float = Float.NEGATIVE_INFINITY,
    @SerialName("t")
    val rotationDistributionParamType: Int = Int.MIN_VALUE,
    @SerialName("a")
    val axis: Int = Int.MIN_VALUE,
    @SerialName("r")
    val flipRotation: Int = Int.MIN_VALUE,
    @SerialName("b")
    val brightnessDistributionShouldAffectFirstBaseEvent: Int = Int.MIN_VALUE,
    @SerialName("l")
    val lightRotationBaseDataList: List<LightRotationBaseData> = listOf()
) : GroupableEventBox

@Serializable
data class LightRotationBaseData(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("p")
    val usePreviousEventRotationValue: Int = Int.MIN_VALUE,
    @SerialName("e")
    val easeType: Int = Int.MIN_VALUE,
    @SerialName("l")
    val loopsCount: Int = Int.MIN_VALUE,
    @SerialName("r")
    val rotation: Float = Float.NEGATIVE_INFINITY,
    @SerialName("o")
    val rotationDirection: Int = Int.MIN_VALUE
)

@Serializable
data class BSIndexFilter(
    @SerialName("f")
    val type: Int = Int.MIN_VALUE,
    @SerialName("p")
    val param0: Int = Int.MIN_VALUE,
    @SerialName("t")
    val param1: Int = Int.MIN_VALUE,
    @SerialName("r")
    val reversed: Int = Int.MIN_VALUE
)

@Serializable
data class BSWaypoint(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE,
    @SerialName("d")
    val offsetDirection: Int = Int.MIN_VALUE
)

@Serializable
data class BSBomb(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE
)

@Serializable
data class BSNoteV3(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE,
    @SerialName("a")
    val angle: Int = Int.MIN_VALUE,
    @SerialName("c")
    val color: Int = Int.MIN_VALUE,
    @SerialName("d")
    val direction: Int = Int.MIN_VALUE
)

@Serializable
data class BSBurstSlider(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("c")
    val color: Int = Int.MIN_VALUE,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE,
    @SerialName("d")
    val direction: Int = Int.MIN_VALUE,
    @SerialName("tb")
    val tailBeat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("tx")
    val tailX: Int = Int.MIN_VALUE,
    @SerialName("ty")
    val tailY: Int = Int.MIN_VALUE,
    @SerialName("sc")
    val sliceCount: Int = Int.MIN_VALUE,
    @SerialName("s")
    val squishAmount: Float = Float.NEGATIVE_INFINITY
)

@Serializable
data class BSSlider(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("c")
    val color: Int = Int.MIN_VALUE,
    val x: Int = Int.MIN_VALUE,
    val y: Int = Int.MIN_VALUE,
    @SerialName("d")
    val direction: Int = Int.MIN_VALUE,
    @SerialName("tb")
    val tailBeat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("tx")
    val tailX: Int = Int.MIN_VALUE,
    @SerialName("ty")
    val tailY: Int = Int.MIN_VALUE,
    @SerialName("mu")
    val headControlPointLengthMultiplier: Float = Float.NEGATIVE_INFINITY,
    @SerialName("tmu")
    val tailControlPointLengthMultiplier: Float = Float.NEGATIVE_INFINITY,
    @SerialName("tc")
    val tailCutDirection: Int = Int.MIN_VALUE,
    @SerialName("m")
    val sliderMidAnchorMode: Int = Int.MIN_VALUE
)

@Serializable
data class BSEventV3(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("et")
    val eventType: Int = Int.MIN_VALUE,
    @SerialName("i")
    val value: Int = Int.MIN_VALUE,
    @SerialName("f")
    val floatValue: Float = Float.NEGATIVE_INFINITY
)

@Serializable
data class BSRotationEvent(
    @SerialName("b")
    val beat: Float = Float.NEGATIVE_INFINITY,
    @SerialName("e")
    val executionTime: Int = Int.MIN_VALUE,
    @SerialName("r")
    val rotation: Float = Float.NEGATIVE_INFINITY
)
