@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject

@Serializable
data class BSDifficulty(
    @SerialName("_version") @ValidationName("_version")
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _notes: OptionalProperty<List<OptionalProperty<BSNote?>>?> = OptionalProperty.NotPresent,
    val _obstacles: OptionalProperty<List<OptionalProperty<BSObstacle?>>?> = OptionalProperty.NotPresent,
    val _events: OptionalProperty<List<OptionalProperty<BSEvent?>>?> = OptionalProperty.NotPresent,
    val _waypoints: OptionalProperty<List<OptionalProperty<BSWaypointV2?>>?> = OptionalProperty.NotPresent,
    val _specialEventsKeywordFilters: OptionalProperty<BSSpecialEventKeywordFilters?> = OptionalProperty.NotPresent,
    override val _customData: OptionalProperty<BSCustomDataV2?> = OptionalProperty.NotPresent,
    val _BPMChanges: OptionalProperty<List<OptionalProperty<BPMChange?>>?> = OptionalProperty.NotPresent
) : BSDiff {
    fun notes() = _notes.orEmpty()
    fun events() = _events.orEmpty()
    fun obstacles() = _obstacles.orEmpty()

    private val noteCountLazy by lazy {
        notes().withoutFake().partition { note -> note._type.orNull() != 3 }
    }
    override fun noteCount() = noteCountLazy.first.size
    override fun bombCount() = noteCountLazy.second.size
    override fun arcCount() = 0
    override fun chainCount() = 0
    override fun obstacleCount() = obstacles().withoutFake().size
    override fun eventCount() = events().size
    private val songLengthLazy by lazy {
        notes().sortedBy { note -> note.time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.last().time - sorted.first().time
            } else {
                0f
            }
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

abstract class BSObject {
    abstract val beat: OptionalProperty<Float?>
    val time by orNegativeInfinity { beat }
}

@Serializable
data class BSNote(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _lineIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _lineLayer: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _cutDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val _customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSCustomData, BSObject() {
    val lineIndex by orMinValue { _lineIndex }
    val lineLayer by orMinValue { _lineLayer }
    val type by orMinValue { _type }
    val cutDirection by orMinValue { _cutDirection }
}

@Serializable
data class BSObstacle(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _lineIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _duration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _width: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val _customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSCustomData, BSObject()

@Serializable
data class BSEvent(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _type: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _value: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val _customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BSCustomData, BSObject()

@Serializable
data class BSCustomDataV2(
    val _BPMChanges: OptionalProperty<List<OptionalProperty<BPMChange?>>?> = OptionalProperty.NotPresent,
    val _time: OptionalProperty<Float?> = OptionalProperty.NotPresent
)

@Serializable
data class BPMChange(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _BPM: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _beatsPerBar: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _metronomeOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSObject()

@Serializable
data class BSWaypointV2(
    @SerialName("_time") @ValidationName("_time")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _lineIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _lineLayer: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _offsetDirection: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSObject()

@Serializable
data class BSSpecialEventKeywordFilters(
    val _keywords: OptionalProperty<List<OptionalProperty<BSSpecialEventsForKeyword?>>?> = OptionalProperty.NotPresent
)

@Serializable
data class BSSpecialEventsForKeyword(
    val _keyword: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _specialEvents: OptionalProperty<List<OptionalProperty<Int?>>?> = OptionalProperty.NotPresent
)
