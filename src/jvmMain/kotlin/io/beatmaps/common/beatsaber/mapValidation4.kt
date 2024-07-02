package io.beatmaps.common.beatsaber

import io.beatmaps.common.or
import io.beatmaps.common.zip.ExtractedInfo

fun BMValidator<BSDifficultyV4>.validateV4(info: ExtractedInfo, diff: BSDifficultyV4, maxBeat: Float, ver: Version) {
    validate(BSDifficultyV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficultyV4::colorNotes).correctType().exists().optionalNotNull().validateForEach { note ->
        validate(BSNoteV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNoteV4::rotationLane).correctType().exists().optionalNotNull()
        validate(BSNoteV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            note.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::colorNotesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSNoteDataV4::x).correctType().exists().optionalNotNull()
        validate(BSNoteDataV4::y).correctType().exists().optionalNotNull()
        validate(BSNoteDataV4::color).correctType().exists().isIn(0, 1)
        validate(BSNoteDataV4::direction).correctType().exists().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNoteDataV4::angleOffset).correctType().exists().optionalNotNull()
    }
    validate(BSDifficultyV4::bombNotes).correctType().exists().optionalNotNull().validateForEach { bomb ->
        validate(BSBombV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBombV4::rotationLane).correctType().exists().optionalNotNull()
        validate(BSBombV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            bomb.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::bombNotesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSBombDataV4::x).correctType().exists().optionalNotNull()
        validate(BSBombDataV4::y).correctType().exists().optionalNotNull()
    }
    validate(BSDifficultyV4::obstacles).correctType().exists().optionalNotNull().validateForEach { obstacle ->
        validate(BSObstacleV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSObstacleV4::rotationLane).correctType().exists().optionalNotNull()
        validate(BSObstacleV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            obstacle.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::obstaclesData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSObstacleDataV4::duration).correctType().exists().optionalNotNull()
        validate(BSObstacleDataV4::x).correctType().exists().optionalNotNull()
        validate(BSObstacleDataV4::y).correctType().exists().optionalNotNull()
        validate(BSObstacleDataV4::width).correctType().exists().optionalNotNull()
        validate(BSObstacleDataV4::height).correctType().exists().optionalNotNull()
    }
    validate(BSDifficultyV4::arcs).correctType().exists().optionalNotNull().validateForEach { arc ->
        validate(BSArcV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, arc.tailBeat.or(maxBeat))
        }
        validate(BSArcV4::tailBeat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(arc.beat.or(0f), maxBeat)
        }
        validate(BSArcV4::headRotationLane).correctType().exists().optionalNotNull()
        validate(BSArcV4::tailRotationLane).correctType().exists().optionalNotNull()
        validate(BSArcV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            arc.getData(diff) != null
        }
        validate(BSArcV4::headIndex).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            arc.getHead(diff) != null
        }
        validate(BSArcV4::tailIndex).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            arc.getTail(diff) != null
        }
    }
    validate(BSDifficultyV4::arcsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSArcDataV4::headControlPointLengthMultiplier).correctType().exists().optionalNotNull()
        validate(BSArcDataV4::tailControlPointLengthMultiplier).correctType().exists().optionalNotNull()
        validate(BSArcDataV4::midAnchorMode).correctType().exists().optionalNotNull()
    }
    validate(BSDifficultyV4::chains).correctType().exists().optionalNotNull().validateForEach { chain ->
        validate(BSChainV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, chain.tailBeat.or(maxBeat))
        }
        validate(BSChainV4::tailBeat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(chain.beat.or(0f), maxBeat)
        }
        validate(BSChainV4::headRotationLane).correctType().exists().optionalNotNull()
        validate(BSChainV4::tailRotationLane).correctType().exists().optionalNotNull()
        validate(BSChainV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            chain.getData(diff) != null
        }
        validate(BSChainV4::headIndex).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            chain.getHead(diff) != null
        }
    }
    validate(BSDifficultyV4::chainsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSChainDataV4::x).correctType().exists().optionalNotNull()
        validate(BSChainDataV4::y).correctType().exists().optionalNotNull()
        validate(BSChainDataV4::sliceCount).correctType().exists().optionalNotNull()
        validate(BSChainDataV4::squishAmount).correctType().exists().optionalNotNull()
    }
    validate(BSDifficultyV4::spawnRotations).correctType().exists().optionalNotNull().validateForEach { rotation ->
        validate(BSRotationsV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSRotationsV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            rotation.getData(diff) != null
        }
    }
    validate(BSDifficultyV4::spawnRotationsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSRotationsDataV4::executionTime).correctType().exists().optionalNotNull()
        validate(BSRotationsDataV4::rotation).correctType().exists().optionalNotNull()
    }
}
