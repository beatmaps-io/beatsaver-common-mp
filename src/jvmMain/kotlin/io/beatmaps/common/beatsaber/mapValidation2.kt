package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator

fun Validator<BSDifficulty>.validate(info: ExtractedInfo, maxBeat: Float) {
    validateSerial(BSDifficulty::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validateSerial(BSDifficulty::_notes).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSNote::_type).correctType().exists().isIn(0, 1, 3)
        validateSerial(BSNote::_cutDirection).correctType().exists().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validateSerial(BSNote::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validateSerial(BSNote::_lineIndex).correctType().exists().optionalNotNull()
        validateSerial(BSNote::_lineLayer).correctType().exists().optionalNotNull()
    }
    validateSerial(BSDifficulty::_obstacles).correctType().exists().optionalNotNull().validateWith(::validateObstacle)
    validateSerial(BSDifficulty::_events).correctType().exists().optionalNotNull().validateWith(::validateEvent)
    validateSerial(BSDifficulty::_waypoints).correctType().optionalNotNull().validateWith(::validateWaypoint)
    validateSerial(BSDifficulty::_specialEventsKeywordFilters).correctType().optionalNotNull().validateOptional {
        validateSerial(BSSpecialEventKeywordFilters::_keywords).correctType().optionalNotNull().validateForEach {
            validateSerial(BSSpecialEventsForKeyword::_keyword).correctType().exists().optionalNotNull()
            validateSerial(BSSpecialEventsForKeyword::_specialEvents).correctType().exists().optionalNotNull().validateEach()
        }
    }
    validateSerial(BSDifficulty::_customData).correctType().optionalNotNull().validateOptional {
        validateSerial(BSCustomDataV2::_time).correctType().optionalNotNull()
        validateSerial(BSCustomDataV2::_BPMChanges).correctType().optionalNotNull().validateWith(::validateBPMChange)
    }
    validateSerial(BSDifficulty::_BPMChanges).correctType().optionalNotNull().validateWith(::validateBPMChange)
}

fun validateObstacle(validator: Validator<BSObstacle>) = validator.apply {
    validateSerial(BSObstacle::_type).correctType().exists().optionalNotNull()
    validateSerial(BSObstacle::_duration).correctType().exists().optionalNotNull()
    validateSerial(BSObstacle::beat).correctType().exists().optionalNotNull()
    validateSerial(BSObstacle::_lineIndex).correctType().exists().optionalNotNull()
    validateSerial(BSObstacle::_width).correctType().exists().optionalNotNull()
}

fun validateEvent(validator: Validator<BSEvent>) = validator.apply {
    validateSerial(BSEvent::beat).correctType().exists().optionalNotNull()
    validateSerial(BSEvent::_type).correctType().exists().optionalNotNull()
    validateSerial(BSEvent::_value).correctType().exists().optionalNotNull()
}

fun validateWaypoint(validator: Validator<BSWaypointV2>) = validator.apply {
    validateSerial(BSWaypointV2::beat).correctType().exists().optionalNotNull()
    validateSerial(BSWaypointV2::_lineIndex).correctType().exists().optionalNotNull()
    validateSerial(BSWaypointV2::_lineLayer).correctType().exists().optionalNotNull()
    validateSerial(BSWaypointV2::_offsetDirection).correctType().exists().optionalNotNull()
}

fun validateBPMChange(validator: Validator<BPMChange>) = validator.apply {
    validateSerial(BPMChange::beat).exists().correctType().optionalNotNull()
    validateSerial(BPMChange::_BPM).exists().correctType().optionalNotNull()
    validateSerial(BPMChange::_beatsPerBar).correctType().optionalNotNull()
    validateSerial(BPMChange::_metronomeOffset).correctType().optionalNotNull()
}
