package io.beatmaps.common.beatsaber.map

import io.beatmaps.common.beatsaber.BMValidator
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.beatsaber.IndexedConstraint
import io.beatmaps.common.beatsaber.Schema4_1
import io.beatmaps.common.beatsaber.Version
import io.beatmaps.common.beatsaber.correctType
import io.beatmaps.common.beatsaber.exists
import io.beatmaps.common.beatsaber.isBetween
import io.beatmaps.common.beatsaber.isIn
import io.beatmaps.common.beatsaber.matches
import io.beatmaps.common.beatsaber.notExistsAfter
import io.beatmaps.common.beatsaber.notExistsBefore
import io.beatmaps.common.beatsaber.optionalNotNull
import io.beatmaps.common.beatsaber.validateForEach
import io.beatmaps.common.or
import io.beatmaps.common.zip.ExtractedInfo

fun BMValidator<BSDifficultyV4>.validateV4(info: ExtractedInfo, diff: BSDifficultyV4, maxBeat: Float, ver: Version) {
    validate(BSDifficultyV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficultyV4::colorNotes).correctType().exists().optionalNotNull().validateForEach { note ->
        validate(BSNoteV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNoteV4::rotationLane).correctType().optionalNotNull()
        validate(BSNoteV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            note.index.orNull() == null || note.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::colorNotesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSNoteDataV4::x).correctType().optionalNotNull()
        validate(BSNoteDataV4::y).correctType().optionalNotNull()
        validate(BSNoteDataV4::color).correctType().isIn(0, 1)
        validate(BSNoteDataV4::direction).correctType().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNoteDataV4::angleOffset).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::bombNotes).correctType().exists().optionalNotNull().validateForEach { bomb ->
        validate(BSBombV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBombV4::rotationLane).correctType().optionalNotNull()
        validate(BSBombV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            bomb.index.orNull() == null || bomb.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::bombNotesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSBombDataV4::x).correctType().optionalNotNull()
        validate(BSBombDataV4::y).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::obstacles).correctType().exists().optionalNotNull().validateForEach { obstacle ->
        validate(BSObstacleV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSObstacleV4::rotationLane).correctType().optionalNotNull()
        validate(BSObstacleV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            obstacle.index.orNull() == null || obstacle.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::obstaclesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSObstacleDataV4::duration).correctType().optionalNotNull()
        validate(BSObstacleDataV4::x).correctType().optionalNotNull()
        validate(BSObstacleDataV4::y).correctType().optionalNotNull()
        validate(BSObstacleDataV4::width).correctType().optionalNotNull()
        validate(BSObstacleDataV4::height).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::arcs).correctType().exists().optionalNotNull().validateForEach { arc ->
        validate(BSArcV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, arc.tailBeat.or(maxBeat))
        }
        validate(BSArcV4::tailBeat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(arc.beat.or(0f), maxBeat)
        }
        validate(BSArcV4::headRotationLane).correctType().optionalNotNull()
        validate(BSArcV4::tailRotationLane).correctType().optionalNotNull()
        validate(BSArcV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            arc.index.orNull() == null || arc.getData(diff) != null
        }
        validate(BSArcV4::headIndex).correctType().optionalNotNull().validate(IndexedConstraint) {
            arc.headIndex.orNull() == null || arc.getHead(diff) != null
        }
        validate(BSArcV4::tailIndex).correctType().optionalNotNull().validate(IndexedConstraint) {
            arc.headIndex.orNull() == null || arc.getTail(diff) != null
        }
    }
    validate(BSDifficultyV4::arcsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSArcDataV4::headControlPointLengthMultiplier).correctType().optionalNotNull()
        validate(BSArcDataV4::tailControlPointLengthMultiplier).correctType().optionalNotNull()
        validate(BSArcDataV4::midAnchorMode).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::chains).correctType().exists().optionalNotNull().validateForEach { chain ->
        validate(BSChainV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, chain.tailBeat.or(maxBeat))
        }
        validate(BSChainV4::tailBeat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(chain.beat.or(0f), maxBeat)
        }
        validate(BSChainV4::headRotationLane).correctType().optionalNotNull()
        validate(BSChainV4::tailRotationLane).correctType().optionalNotNull()
        validate(BSChainV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            chain.index.orNull() == null || chain.getData(diff) != null
        }
        validate(BSChainV4::headIndex).correctType().optionalNotNull().validate(IndexedConstraint) {
            chain.headIndex.orNull() == null || chain.getHead(diff) != null
        }
    }
    validate(BSDifficultyV4::chainsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSChainDataV4::x).correctType().optionalNotNull()
        validate(BSChainDataV4::y).correctType().optionalNotNull()
        validate(BSChainDataV4::sliceCount).correctType().optionalNotNull()
        validate(BSChainDataV4::squishAmount).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::spawnRotations).notExistsAfter(ver, Schema4_1).correctType().optionalNotNull().validateForEach { rotation ->
        validate(BSRotationsV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSRotationsV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            rotation.index.orNull() == null || rotation.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::spawnRotationsData).notExistsAfter(ver, Schema4_1).correctType().optionalNotNull().validateForEach {
        validate(BSRotationsDataV4::executionTime).correctType().optionalNotNull()
        validate(BSRotationsDataV4::rotation).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::njsEvents).notExistsBefore(ver, Schema4_1).correctType().optionalNotNull().validateForEach { njs ->
        validate(BSNjsEventV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNjsEventV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            njs.index.orNull() == null || njs.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::njsEventData).notExistsBefore(ver, Schema4_1).correctType().optionalNotNull().validateForEach {
        validate(BSNjsEventDataV4::relativeNoteJumpSpeed).correctType().optionalNotNull()
        validate(BSNjsEventDataV4::usePreviousValue).correctType().optionalNotNull()
        validate(BSNjsEventDataV4::type).correctType().optionalNotNull()
    }
    validate(BSDifficultyV4::customData).correctType().optionalNotNull()
}
