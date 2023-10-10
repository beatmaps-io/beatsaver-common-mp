package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import kotlin.reflect.KProperty1

fun Validator<BSDifficultyV3>.validateV3(info: ExtractedInfo, maxBeat: Float, ver: Version) {
    validate(BSDifficultyV3::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficultyV3::bpmEvents).correctType().exists().validateForEach {
        validate(BSBpmChange::bpm).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBpmChange::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::rotationEvents).correctType().exists().validateForEach {
        validate(BSRotationEvent::executionTime).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSRotationEvent::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSRotationEvent::rotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorNotes).correctType().exists().validateForEach {
        validate(BSNoteV3::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSNoteV3::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNoteV3::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }

        validate(BSNoteV3::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::angleOffset).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::bombNotes).correctType().exists().validateForEach {
        validate(BSBomb::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::obstacles).correctType().exists().validateForEach {
        validate(BSObstacleV3::duration).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::width).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::height).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::sliders).correctType().exists().validateForEach {
        validate(BSSlider::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSSlider::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailBeat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailX).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailY).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::headControlPointLengthMultiplier).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailControlPointLengthMultiplier).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailCutDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::sliderMidAnchorMode).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::burstSliders).correctType().exists().validateForEach {
        validate(BSBurstSlider::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBurstSlider::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSBurstSlider::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailBeat).correctType().correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBurstSlider::tailX).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailY).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::sliceCount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::squishAmount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::waypoints).correctType().exists().validateForEach {
        validate(BSWaypoint::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::offsetDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::basicBeatmapEvents).correctType().exists().validateForEach {
        validate(BSEventV3::_time).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::eventType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::value).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::floatValue).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorBoostBeatmapEvents).correctType().exists().validateForEach {
        validate(BSBoostEvent::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBoostEvent::boost).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::lightColorEventBoxGroups).correctType().exists().validateForEach {
        validate(BSLightColorEventBoxGroup::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::groupId).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSLightColorEventBox::indexFilter, ver)
            validate(BSLightColorEventBox::beatDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::beatDistributionParamType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(1, 2)
            validate(BSLightColorEventBox::brightnessDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionParamType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(1, 2)
            validate(BSLightColorEventBox::brightnessDistributionShouldAffectFirstBaseEvent).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::lightColorBaseDataList).correctType().optionalNotNull().validateForEach {
                validate(BSLightColorBaseData::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::transitionType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(0, 1, 2)
                validate(BSLightColorBaseData::colorType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(-1, 0, 1, 2)
                validate(BSLightColorBaseData::brightness).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::strobeFrequency).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::lightRotationEventBoxGroups).correctType().exists().validateForEach {
        validate(BSLightRotationEventBoxGroup::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::groupId).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSLightRotationEventBox::indexFilter, ver)
            validate(BSLightRotationEventBox::beatDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::beatDistributionParamType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(1, 2)
            validate(BSLightRotationEventBox::rotationDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::rotationDistributionParamType).correctType().existsBefore(ver, Schema3_3).optionalNotNull().isIn(1, 2)
            validate(BSLightRotationEventBox::axis).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::flipRotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::brightnessDistributionShouldAffectFirstBaseEvent).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::lightRotationBaseDataList).correctType().optionalNotNull().validateForEach {
                validate(LightRotationBaseData::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::usePreviousEventRotationValue).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::easeType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::loopsCount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotationDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::lightTranslationEventBoxGroups).correctType().onlyExistsAfter(ver, Schema3_2).validateForEach {
        validate(BSLightTranslationEventBoxGroup::beat).correctType().optionalNotNull()
        validate(BSLightTranslationEventBoxGroup::groupId).correctType().optionalNotNull()
        validate(BSLightTranslationEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSLightTranslationEventBox::indexFilter, ver)
            validate(BSLightTranslationEventBox::gapDistributionParam).correctType().optionalNotNull()
            validate(BSLightTranslationEventBox::gapDistributionParamType).correctType().optionalNotNull().isIn(1, 2)
            validate(BSLightTranslationEventBox::axis).correctType().optionalNotNull()
            validate(BSLightTranslationEventBox::flipTranslation).correctType().optionalNotNull()
            validate(BSLightTranslationEventBox::gapDistributionShouldAffectFirstBaseEvent).correctType().optionalNotNull()
            validate(BSLightTranslationEventBox::gapDistributionEaseType).correctType().optionalNotNull()
            validate(BSLightTranslationEventBox::lightTranslationBaseDataList).correctType().optionalNotNull()
        }
    }
    validate(BSDifficultyV3::vfxEventBoxGroups).correctType().onlyExistsAfter(ver, Schema3_3).validateForEach {
        validate(BSVfxEventBoxGroup::beat).correctType().optionalNotNull()
        validate(BSVfxEventBoxGroup::groupId).correctType().optionalNotNull()
        validate(BSVfxEventBoxGroup::type).correctType().optionalNotNull().isIn(0, 1, 2)
        validate(BSVfxEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSVfxEventBox::indexFilter, ver)
            validate(BSVfxEventBox::beatDistributionParam).correctType().optionalNotNull()
            validate(BSVfxEventBox::beatDistributionParamType).correctType().optionalNotNull().isIn(1, 2)
            validate(BSVfxEventBox::vfxDistributionParam).correctType().optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionParamType).correctType().optionalNotNull().isIn(1, 2)
            validate(BSVfxEventBox::vfxDistributionEaseType).correctType().optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionShouldAffectFirstBaseEvent).correctType().optionalNotNull()
            validate(BSVfxEventBox::vfxBaseDataList).correctType().optionalNotNull()
        }
    }
    validate(BSDifficultyV3::_fxEventsCollection).correctType().onlyExistsAfter(ver, Schema3_3).validateOptional {
        validate(BSFxEventsCollection::intEventsList).correctType().exists().validateForEach {
            validate(BSIntFxEventBaseData::_time).correctType().optionalNotNull()
            validate(BSIntFxEventBaseData::usePreviousEventValue).correctType().optionalNotNull()
            validate(BSIntFxEventBaseData::value).correctType().optionalNotNull()
        }
        validate(BSFxEventsCollection::floatEventsList).correctType().exists().validateForEach {
            validate(BSFloatFxEventBaseData::_time).correctType().optionalNotNull()
            validate(BSFloatFxEventBaseData::usePreviousEventValue).correctType().optionalNotNull()
            validate(BSFloatFxEventBaseData::value).correctType().optionalNotNull()
            validate(BSFloatFxEventBaseData::easeType).correctType().optionalNotNull()
        }
    }
    validate(BSDifficultyV3::basicEventTypesWithKeywords).correctType().exists().optionalNotNull()
    validate(BSDifficultyV3::useNormalEventsAsCompatibleEvents).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
}

fun <T : GroupableEventBox> Validator<T>.validateEventBox(indexFilter: KProperty1<T, OptionalProperty<BSIndexFilter?>>, ver: Version) {
    validate(indexFilter).exists().validateOptional {
        validate(BSIndexFilter::type).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param0).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param1).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::reversed).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        listOf(
            BSIndexFilter::chunks,
            BSIndexFilter::randomType,
            BSIndexFilter::seed,
            BSIndexFilter::limit,
            BSIndexFilter::alsoAffectsType
        ).forEach { validate(it).correctType().notExistsBefore(ver, Schema3_1).existsBetween(ver, Schema3_1, Schema3_3).optionalNotNull() }
    }
}
