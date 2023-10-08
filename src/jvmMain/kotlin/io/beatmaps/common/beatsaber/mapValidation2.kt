package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import org.valiktor.functions.matches

fun Validator<BSDifficulty>.validate(info: ExtractedInfo, maxBeat: Float) {
    validate(BSDifficulty::version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficulty::_notes).exists().optionalNotNull().validateForEach {
        validate(BSNote::_type).exists().isIn(0, 1, 3)
        validate(BSNote::_cutDirection).exists().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNote::_time).exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNote::_lineIndex).exists().optionalNotNull()
        validate(BSNote::_lineLayer).exists().optionalNotNull()
    }
    validate(BSDifficulty::_obstacles).exists().optionalNotNull().validateForEach {
        validate(BSObstacle::_type).exists().optionalNotNull()
        validate(BSObstacle::_duration).exists().optionalNotNull()
        validate(BSObstacle::_time).exists().optionalNotNull()
        validate(BSObstacle::_lineIndex).exists().optionalNotNull()
        validate(BSObstacle::_width).exists().optionalNotNull()
    }
    validate(BSDifficulty::_events).exists().optionalNotNull().validateForEach {
        validate(BSEvent::_time).exists().optionalNotNull()
        validate(BSEvent::_type).exists().optionalNotNull()
        validate(BSEvent::_value).exists().optionalNotNull()
    }
}