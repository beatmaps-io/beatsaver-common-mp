package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.functions.matches
import org.valiktor.functions.validateForEach
import org.valiktor.validate
import kotlin.math.roundToInt

data class BPMInfo(
    val _version: String,
    val _songSampleCount: Int,
    val _songFrequency: Int,
    val _regions: List<BPMRegion>
) : SongLengthInfo {
    fun validate() = validate(this) {
        validate(BPMInfo::_version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(BPMInfo::_songSampleCount).isPositiveOrZero()
        validate(BPMInfo::_songFrequency).isPositiveOrZero()
        validate(BPMInfo::_regions).validateForEach { it.validate(this, this@BPMInfo) }
    }

    private fun duration() = samplesToDuration(_songSampleCount)

    private fun samplesToDuration(samples: Int) = samples / _songFrequency.toFloat()
    private fun durationToSamples(duration: Float) = (duration * _songFrequency).roundToInt()

    private fun maximumInfo() = _regions.maxByOrNull { it._endBeat }?.let {
        // Find the last region, time after is at song's bpm
        it._endBeat to samplesToDuration(_songSampleCount - it._endSampleIndex)
    } ?: (0f to duration())

    override fun maximumBeat(bpm: Float) = maximumInfo().let {
        it.first + ((it.second / 60) * bpm)
    }

    override fun secondsToTime(sec: Float) =
        _regions.find { it._startSampleIndex < durationToSamples(sec) && durationToSamples(sec) < it._endSampleIndex }?.let {
            // We're in this region. Interpolate!
            val lengthInSamples = it._endSampleIndex - it._startSampleIndex
            val percent = (durationToSamples(sec) - it._startSampleIndex) / lengthInSamples
            val lengthInBeats = it._endBeat - it._startBeat
            it._startBeat + (lengthInBeats * percent)
        } ?: 0f

    override fun timeToSeconds(time: Float) =
        _regions.find { it._startBeat <= time && time < it._endBeat }?.let {
            // We're in this region. Interpolate!
            val lengthInBeats = it._endBeat - it._startBeat
            val percent = (time - it._startBeat) / lengthInBeats
            val lengthInSamples = it._endSampleIndex - it._startSampleIndex
            samplesToDuration(it._startSampleIndex + (lengthInSamples * percent).roundToInt())
        } ?: 0f
}

class LegacySongLengthInfo(private val info: ExtractedInfo) : SongLengthInfo {
    override fun maximumBeat(bpm: Float) = secondsToTime(info.duration)
    override fun timeToSeconds(time: Float) = (time / info.mapInfo._beatsPerMinute) * 60
    override fun secondsToTime(sec: Float) = (sec / 60) * info.mapInfo._beatsPerMinute
}

data class BPMRegion(
    val _startSampleIndex: Int,
    val _endSampleIndex: Int,
    val _startBeat: Float,
    val _endBeat: Float
) {
    fun validate(validator: Validator<BPMRegion>, bpmInfo: BPMInfo) = validator.apply {
        validate(BPMRegion::_startSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo._songSampleCount)
        validate(BPMRegion::_endSampleIndex).isPositiveOrZero().isLessThanOrEqualTo(bpmInfo._songSampleCount)
        validate(BPMRegion::_startBeat).isPositiveOrZero()
        validate(BPMRegion::_endBeat).isPositiveOrZero()
    }
}
