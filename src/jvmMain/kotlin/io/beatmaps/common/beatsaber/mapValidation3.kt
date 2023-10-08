package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import kotlin.reflect.KProperty1

fun Validator<BSDifficultyV3>.validateV3(info: ExtractedInfo, maxBeat: Float, ver: Version) {
    validate(BSDifficultyV3::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficultyV3::bpmEvents).exists().validateForEach {
        validate(BSBpmChange::bpm).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBpmChange::beat).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::rotationEvents).exists().validateForEach {
        validate(BSRotationEvent::executionTime).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSRotationEvent::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSRotationEvent::rotation).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorNotes).exists().validateForEach {
        validate(BSNoteV3::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSNoteV3::direction).existsBefore(ver, Schema3_3).optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNoteV3::_time).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }

        validate(BSNoteV3::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::angleOffset).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::bombNotes).exists().validateForEach {
        validate(BSBomb::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::y).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::obstacles).exists().validateForEach {
        validate(BSObstacleV3::duration).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::width).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::height).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::sliders).exists().validateForEach {
        validate(BSSlider::_time).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSSlider::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::direction).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailBeat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailX).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailY).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::headControlPointLengthMultiplier).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailControlPointLengthMultiplier).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailCutDirection).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::sliderMidAnchorMode).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::burstSliders).exists().validateForEach {
        validate(BSBurstSlider::_time).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }
        validate(BSBurstSlider::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSBurstSlider::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::direction).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailBeat).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }
        validate(BSBurstSlider::tailX).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailY).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::sliceCount).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::squishAmount).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::waypoints).exists().validateForEach {
        validate(BSWaypoint::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::offsetDirection).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::basicBeatmapEvents).exists().validateForEach {
        validate(BSEventV3::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::eventType).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::value).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::floatValue).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorBoostBeatmapEvents).exists().validateForEach {
        validate(BSBoostEvent::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBoostEvent::boost).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::lightColorEventBoxGroups).exists().validateForEach {
        validate(BSLightColorEventBoxGroup::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::groupId).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSLightColorEventBox::indexFilter, ver)
            validate(BSLightColorEventBox::beatDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::beatDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionShouldAffectFirstBaseEvent).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::lightColorBaseDataList).optionalNotNull().validateForEach {
                validate(BSLightColorBaseData::beat).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::transitionType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::colorType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::colorType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::brightness).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::strobeFrequency).existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::lightRotationEventBoxGroups).exists().validateForEach {
        validate(BSLightRotationEventBoxGroup::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::groupId).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSLightRotationEventBox::indexFilter, ver)
            validate(BSLightRotationEventBox::beatDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::beatDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::rotationDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::rotationDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::axis).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::flipRotation).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::brightnessDistributionShouldAffectFirstBaseEvent).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::lightRotationBaseDataList).isNotNull().validateForEach {
                validate(LightRotationBaseData::beat).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::usePreviousEventRotationValue).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::easeType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::loopsCount).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotation).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotationDirection).existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::vfxEventBoxGroups).onlyExistsAfter(ver, Schema3_3).validateForEach {
        validate(BSVfxEventBoxGroup::beat).optionalNotNull()
        validate(BSVfxEventBoxGroup::groupId).optionalNotNull()
        validate(BSVfxEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSVfxEventBox::indexFilter, ver)
            validate(BSVfxEventBox::beatDistributionParam).optionalNotNull()
            validate(BSVfxEventBox::beatDistributionParamType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionParam).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionParamType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionEaseType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionShouldAffectFirstBaseEvent).optionalNotNull()
            validate(BSVfxEventBox::vfxBaseDataList).optionalNotNull()
        }
    }
    validate(BSDifficultyV3::_fxEventsCollection).onlyExistsAfter(ver, Schema3_3).validateOptional {
        validate(BSFxEventsCollection::intEventsList).exists()
        validate(BSFxEventsCollection::floatEventsList).exists()
    }
    validate(BSDifficultyV3::basicEventTypesWithKeywords).existsBefore(ver, Schema3_3).optionalNotNull()
    validate(BSDifficultyV3::useNormalEventsAsCompatibleEvents).existsBefore(ver, Schema3_3).optionalNotNull()
}

fun <T : GroupableEventBox> Validator<T>.validateEventBox(indexFilter: KProperty1<T, OptionalProperty<BSIndexFilter?>>, ver: Version) {
    validate(indexFilter).exists().validateOptional {
        validate(BSIndexFilter::type).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param0).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param1).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::reversed).existsBefore(ver, Schema3_3).optionalNotNull()
        listOf(
            BSIndexFilter::chunks,
            BSIndexFilter::randomType,
            BSIndexFilter::seed,
            BSIndexFilter::limit,
            BSIndexFilter::alsoAffectsType
        ).map { validate(it) }.forEach { it.notExistsBefore(ver, Schema3_1).existsBetween(ver, Schema3_1, Schema3_3).optionalNotNull() }
    }
}