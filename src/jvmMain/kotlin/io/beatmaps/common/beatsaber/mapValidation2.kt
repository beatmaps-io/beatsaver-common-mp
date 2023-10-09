package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator

fun Validator<BSDifficulty>.validate(info: ExtractedInfo, maxBeat: Float) {
    validate(BSDifficulty::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficulty::_notes).correctType().exists().optionalNotNull().validateForEach {
        validate(BSNote::_type).correctType().exists().isIn(0, 1, 3)
        validate(BSNote::_cutDirection).correctType().exists().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNote::_time).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNote::_lineIndex).correctType().exists().optionalNotNull()
        validate(BSNote::_lineLayer).correctType().exists().optionalNotNull()
    }
    validate(BSDifficulty::_obstacles).correctType().exists().optionalNotNull().validateForEach {
        validate(BSObstacle::_type).correctType().exists().optionalNotNull()
        validate(BSObstacle::_duration).correctType().exists().optionalNotNull()
        validate(BSObstacle::_time).correctType().exists().optionalNotNull()
        validate(BSObstacle::_lineIndex).correctType().exists().optionalNotNull()
        validate(BSObstacle::_width).correctType().exists().optionalNotNull()
    }
    validate(BSDifficulty::_events).correctType().exists().optionalNotNull().validateForEach {
        validate(BSEvent::_time).correctType().exists().optionalNotNull()
        validate(BSEvent::_type).correctType().exists().optionalNotNull()
        validate(BSEvent::_value).correctType().exists().optionalNotNull()
    }
    validate(BSDifficulty::_waypoints).correctType().optionalNotNull()
    validate(BSDifficulty::_specialEventsKeywordFilters).correctType().optionalNotNull()
    validate(BSDifficulty::_customData).correctType().optionalNotNull()
    validate(BSDifficulty::_BPMChanges).correctType().optionalNotNull()
}
