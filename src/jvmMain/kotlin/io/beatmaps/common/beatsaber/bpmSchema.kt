package io.beatmaps.common.beatsaber

import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.valiktor.Validator
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.functions.matches
import org.valiktor.functions.validateForEach
import org.valiktor.validate
import kotlin.math.roundToInt

@Serializable
data class BPMInfo(
    @SerialName("_version") override val version: String,
    @SerialName("_songSampleCount") override val songSampleCount: Int,
    @SerialName("_songFrequency") override val songFrequency: Int,
    @SerialName("_regions") override val bpmData: List<BPMRegion>
) : BPMInfoBase() {
    override fun validate() = validate(this) {
        validate(BPMInfo::version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(BPMInfo::songSampleCount).isPositiveOrZero()
        validate(BPMInfo::songFrequency).isPositiveOrZero()
        validate(BPMInfo::bpmData).validateForEach { it.validate(this, this@BPMInfo) }
    }
}

@Serializable
data class BPMInfoV4(
    override val version: String,
    val songChecksum: String,
    override val songSampleCount: Int,
    override val songFrequency: Int,
    override val bpmData: List<BPMRegionV4>
) : BPMInfoBase() {
    override fun validate() = validate(this) {
        validate(BPMInfoV4::version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(BPMInfoV4::songChecksum).isNotNull()
        validate(BPMInfoV4::songSampleCount).isPositiveOrZero()
        validate(BPMInfoV4::songFrequency).isPositiveOrZero()
        validate(BPMInfoV4::bpmData).validateForEach { it.validate(this, this@BPMInfoV4) }
    }
}

abstract class BPMInfoBase : SongLengthInfo {
    abstract val version: String
    abstract val songFrequency: Int
    abstract val songSampleCount: Int
    abstract val bpmData: List<IBPMRegion<*>>

    abstract fun validate(): BPMInfoBase

    private fun maximumInfo() = bpmData.maxByOrNull { it.endBeat }?.let {
        // Find the last region, time after is at song's bpm
        it.endBeat to samplesToDuration(songSampleCount - it.endSampleIndex)
    } ?: (0f to duration())

    override fun maximumBeat(bpm: Float) = maximumInfo().let {
        it.first + ((it.second / 60) * bpm)
    }

    override fun secondsToTime(sec: Float) =
        bpmData.find { it.startSampleIndex < durationToSamples(sec) && durationToSamples(sec) < it.endSampleIndex }?.let {
            // We're in this region. Interpolate!
            val lengthInSamples = it.endSampleIndex - it.startSampleIndex
            val percent = (durationToSamples(sec) - it.startSampleIndex) / lengthInSamples
            val lengthInBeats = it.endBeat - it.startBeat
            it.startBeat + (lengthInBeats * percent)
        } ?: 0f

    override fun timeToSeconds(time: Float) =
        bpmData.find { it.startBeat <= time && time < it.endBeat }?.let {
            // We're in this region. Interpolate!
            val lengthInBeats = it.endBeat - it.startBeat
            val percent = (time - it.startBeat) / lengthInBeats
            val lengthInSamples = it.endSampleIndex - it.startSampleIndex
            samplesToDuration(it.startSampleIndex + (lengthInSamples * percent).roundToInt())
        } ?: 0f

    protected fun duration() = samplesToDuration(songSampleCount)
    private fun samplesToDuration(samples: Int) = samples / songFrequency.toFloat()
    private fun durationToSamples(duration: Float) = (duration * songFrequency).roundToInt()

    companion object {
        fun parse(element: JsonElement) =
            if (element.jsonObject.containsKey("version")) {
                jsonIgnoreUnknown.decodeFromJsonElement<BPMInfoV4>(element)
            } else {
                jsonIgnoreUnknown.decodeFromJsonElement<BPMInfo>(element)
            }
    }
}

class LegacySongLengthInfo(private val info: ExtractedInfo) : SongLengthInfo {
    override fun maximumBeat(bpm: Float) = secondsToTime(info.duration)
    override fun timeToSeconds(time: Float) = (time / (info.mapInfo.getBpm() ?: 1f)) * 60
    override fun secondsToTime(sec: Float) = (sec / 60) * (info.mapInfo.getBpm() ?: 1f)
}

@Serializable
data class BPMRegion(
    @SerialName("_startSampleIndex") override val startSampleIndex: Int,
    @SerialName("_endSampleIndex") override val endSampleIndex: Int,
    @SerialName("_startBeat") override val startBeat: Float,
    @SerialName("_endBeat") override val endBeat: Float
) : IBPMRegion<BPMRegion> {
    override fun validate(validator: Validator<BPMRegion>, bpmInfo: BPMInfoBase) = validator.apply {
        validate(BPMRegion::startSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo.songSampleCount)
        validate(BPMRegion::endSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo.songSampleCount)
        validate(BPMRegion::startBeat).isPositiveOrZero()
        validate(BPMRegion::endBeat).isPositiveOrZero()
    }
}

@Serializable
data class BPMRegionV4(
    @SerialName("si") override val startSampleIndex: Int,
    @SerialName("ei") override val endSampleIndex: Int,
    @SerialName("sb") override val startBeat: Float,
    @SerialName("eb") override val endBeat: Float
) : IBPMRegion<BPMRegionV4> {
    override fun validate(validator: Validator<BPMRegionV4>, bpmInfo: BPMInfoBase) = validator.apply {
        validate(BPMRegionV4::startSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo.songSampleCount)
        validate(BPMRegionV4::endSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo.songSampleCount)
        validate(BPMRegionV4::startBeat).isPositiveOrZero()
        validate(BPMRegionV4::endBeat).isPositiveOrZero()
    }
}

interface IBPMRegion<T : IBPMRegion<T>> {
    val startSampleIndex: Int
    val endSampleIndex: Int
    val startBeat: Float
    val endBeat: Float

    fun validate(validator: Validator<T>, bpmInfo: BPMInfoBase): Validator<T>
}
