package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.functions.matches
import org.valiktor.functions.validateForEach
import org.valiktor.validate

interface SongLengthInfo {
    fun maximumBeat(bpm: Float): Float
}

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

    fun duration() = samplesToDuration(_songSampleCount)

    private fun samplesToDuration(samples: Int) = samples / _songFrequency.toFloat()

    override fun maximumBeat(bpm: Float) = (_regions.maxByOrNull { it._endBeat }?.let {
        // Find the last region, time after is at song's bpm
        it._endBeat to samplesToDuration(_songSampleCount - it._endSampleIndex)
    } ?: (0f to duration())).let {
        it.first + ((it.second / 60) * bpm)
    }
}

class LegacySongLengthInfo(val info: ExtractedInfo) : SongLengthInfo {
    override fun maximumBeat(bpm: Float) = (info.duration / 60) * info.mapInfo._beatsPerMinute
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
