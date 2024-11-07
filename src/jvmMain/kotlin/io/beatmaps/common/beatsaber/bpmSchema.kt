@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.or
import io.beatmaps.common.zip.ExtractedInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.math.roundToInt

@Serializable
data class BPMInfo(
    @SerialName("_version") @ValidationName("_version")
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_songSampleCount") @ValidationName("_songSampleCount")
    override val songSampleCount: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_songFrequency") @ValidationName("_songFrequency")
    override val songFrequency: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_regions") @ValidationName("_regions")
    override val bpmData: OptionalProperty<List<OptionalProperty<BPMRegion?>>?> = OptionalProperty.NotPresent
) : BPMInfoBase() {
    override fun validate() = validate(this) {
        validate(BPMInfo::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(BPMInfo::songSampleCount).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMInfo::songFrequency).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMInfo::bpmData).correctType().exists().optionalNotNull().validateForEach { it.validate(this, this@BPMInfo) }
    }
}

@Serializable
data class BPMInfoV4(
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val songChecksum: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val songSampleCount: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val songFrequency: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val bpmData: OptionalProperty<List<OptionalProperty<BPMRegionV4?>>?> = OptionalProperty.NotPresent
) : BPMInfoBase() {
    override fun validate() = validate(this) {
        validate(BPMInfoV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(BPMInfoV4::songChecksum).correctType().exists().optionalNotNull()
        validate(BPMInfoV4::songSampleCount).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMInfoV4::songFrequency).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMInfoV4::bpmData).correctType().exists().optionalNotNull().validateForEach { it.validate(this, this@BPMInfoV4) }
    }
}

abstract class BPMInfoBase : SongLengthInfo {
    abstract val version: OptionalProperty<String?>
    abstract val songFrequency: OptionalProperty<Int?>
    abstract val songSampleCount: OptionalProperty<Int?>
    abstract val bpmData: OptionalProperty<List<OptionalProperty<IBPMRegion<*>?>>?>

    abstract fun validate(): BPMInfoBase

    private fun maximumInfo() = bpmData.orEmpty().maxByOrNull { it.endBeat.or(0f) }?.let {
        // Find the last region, time after is at song's bpm
        it.endBeat.or(0f) to samplesToDuration(songSampleCount.or(0) - it.endSampleIndex.or(0))
    } ?: (0f to duration())

    override fun maximumBeat(bpm: Float) = maximumInfo().let {
        it.first + ((it.second / 60) * bpm)
    }

    override fun secondsToTime(sec: Float) =
        bpmData.orEmpty().find { it.startSampleIndex.or(0) < durationToSamples(sec) && durationToSamples(sec) < it.endSampleIndex.or(0) }?.let {
            // We're in this region. Interpolate!
            val lengthInSamples = it.endSampleIndex.or(0) - it.startSampleIndex.or(0)
            val percent = (durationToSamples(sec) - it.startSampleIndex.or(0)) / lengthInSamples
            val lengthInBeats = it.endBeat.or(0f) - it.startBeat.or(0f)
            it.startBeat.or(0f) + (lengthInBeats * percent)
        } ?: 0f

    override fun timeToSeconds(time: Float) =
        bpmData.orEmpty().find { it.startBeat.or(0f) <= time && time < it.endBeat.or(0f) }?.let {
            // We're in this region. Interpolate!
            val lengthInBeats = it.endBeat.or(0f) - it.startBeat.or(0f)
            val percent = (time - it.startBeat.or(0f)) / lengthInBeats
            val lengthInSamples = it.endSampleIndex.or(0) - it.startSampleIndex.or(0)
            samplesToDuration(it.startSampleIndex.or(0) + (lengthInSamples * percent).roundToInt())
        } ?: 0f

    protected fun duration() = samplesToDuration(songSampleCount.or(0))
    private fun samplesToDuration(samples: Int) = samples / songFrequency.or(1).toFloat()
    private fun durationToSamples(duration: Float) = (duration * songFrequency.or(0)).roundToInt()

    override fun withBpmEvents(events: List<BSBpmChange>) = this

    companion object {
        fun parse(element: JsonElement) =
            element.jsonObject.parseBS({
                jsonIgnoreUnknown.decodeFromJsonElement<BPMInfoV4>(element)
            }) {
                jsonIgnoreUnknown.decodeFromJsonElement<BPMInfo>(element)
            }
    }
}

class LegacySongLengthInfo(private val info: ExtractedInfo) : SongLengthInfo {
    override fun maximumBeat(bpm: Float) = secondsToTime(info.duration)
    override fun timeToSeconds(time: Float) = (time / (info.mapInfo.getBpm() ?: 1f)) * 60
    override fun secondsToTime(sec: Float) = (sec / 60) * (info.mapInfo.getBpm() ?: 1f)

    override fun withBpmEvents(events: List<BSBpmChange>) = BPMChangeLengthInfo(info, events)
}

data class BpmTracker(val seconds: Float, val beat: Float, val bpm: Float)

class BPMChangeLengthInfo(private val info: ExtractedInfo, private val events: List<BSBpmChange>) : SongLengthInfo {
    private val trackers = events.fold(listOf(BpmTracker(0f, 0f, info.mapInfo.getBpm() ?: 60f))) { list, newBpm ->
        val oldData = list.last()
        list.plus(
            BpmTracker(
                oldData.seconds + (((newBpm.beat.or(0f) - oldData.beat) / oldData.bpm) * 60f),
                newBpm.beat.or(oldData.beat),
                newBpm.bpm.or(60f)
            )
        )
    }

    override fun maximumBeat(bpm: Float) =
        trackers.last().let { end -> end.beat + (((info.duration - end.seconds) / 60f) * end.bpm) }

    override fun timeToSeconds(time: Float) =
        trackers.last { it.beat <= time }.let { previousBpmEvent ->
            previousBpmEvent.seconds + (((time - previousBpmEvent.beat) / previousBpmEvent.bpm) * 60f)
        }

    override fun secondsToTime(sec: Float) =
        trackers.last { it.seconds <= sec }.let { previousBpmEvent ->
            previousBpmEvent.beat + (((sec - previousBpmEvent.seconds) / 60f) * previousBpmEvent.bpm)
        }

    override fun withBpmEvents(events: List<BSBpmChange>) = BPMChangeLengthInfo(info, events)
}

@Serializable
data class BPMRegion(
    @SerialName("_startSampleIndex") @ValidationName("_startSampleIndex")
    override val startSampleIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_endSampleIndex") @ValidationName("_endSampleIndex")
    override val endSampleIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_startBeat") @ValidationName("_startBeat")
    override val startBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("_endBeat") @ValidationName("_endBeat")
    override val endBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBPMRegion<BPMRegion> {
    override fun validate(validator: BMValidator<BPMRegion>, bpmInfo: BPMInfoBase) = validator.apply {
        validate(BPMRegion::startSampleIndex).correctType().exists().optionalNotNull()
            .isPositiveOrZero()
        validate(BPMRegion::endSampleIndex).correctType().exists().optionalNotNull()
            .isGreaterThanOrEqualTo(startSampleIndex.or(0)).isLessThanOrEqualTo(bpmInfo.songSampleCount.or(0))
        validate(BPMRegion::startBeat).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMRegion::endBeat).correctType().exists().optionalNotNull().isGreaterThanOrEqualTo(startBeat.or(0f))
    }
}

@Serializable
data class BPMRegionV4(
    @SerialName("si") override val startSampleIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ei") override val endSampleIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("sb") override val startBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("eb") override val endBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : IBPMRegion<BPMRegionV4> {
    override fun validate(validator: BMValidator<BPMRegionV4>, bpmInfo: BPMInfoBase) = validator.apply {
        validate(BPMRegionV4::startSampleIndex).correctType().exists().optionalNotNull()
            .isPositiveOrZero()
        validate(BPMRegionV4::endSampleIndex).correctType().exists().optionalNotNull()
            .isGreaterThanOrEqualTo(startSampleIndex.or(0)).isLessThanOrEqualTo(bpmInfo.songSampleCount.or(0))
        validate(BPMRegionV4::startBeat).correctType().exists().optionalNotNull().isPositiveOrZero()
        validate(BPMRegionV4::endBeat).correctType().exists().optionalNotNull().isGreaterThanOrEqualTo(startBeat.or(0f))
    }
}

interface IBPMRegion<T : IBPMRegion<T>> {
    val startSampleIndex: OptionalProperty<Int?>
    val endSampleIndex: OptionalProperty<Int?>
    val startBeat: OptionalProperty<Float?>
    val endBeat: OptionalProperty<Float?>

    fun validate(validator: BMValidator<T>, bpmInfo: BPMInfoBase): BMValidator<T>
}
