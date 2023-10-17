package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.zip.ExtractedInfo
import org.valiktor.Validator
import kotlin.reflect.KProperty1

fun Validator<BSDifficultyV3>.validateV3(info: ExtractedInfo, maxBeat: Float, ver: Version) {
    validateSerial(BSDifficultyV3::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validateSerial(BSDifficultyV3::bpmEvents).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSBpmChange::bpm).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBpmChange::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::rotationEvents).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSRotationEvent::executionTime).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validateSerial(BSRotationEvent::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSRotationEvent::rotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::colorNotes).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSNoteV3::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validateSerial(BSNoteV3::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validateSerial(BSNoteV3::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }

        validateSerial(BSNoteV3::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSNoteV3::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSNoteV3::angleOffset).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::bombNotes).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSBomb::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBomb::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBomb::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::obstacles).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSObstacleV3::duration).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSObstacleV3::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSObstacleV3::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSObstacleV3::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSObstacleV3::width).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSObstacleV3::height).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::sliders).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSSlider::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validateSerial(BSSlider::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::tailBeat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::tailX).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::tailY).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::headControlPointLengthMultiplier).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::tailControlPointLengthMultiplier).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::tailCutDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSSlider::sliderMidAnchorMode).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::burstSliders).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSBurstSlider::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validateSerial(BSBurstSlider::color).correctType().existsBefore(ver, Schema3_3).isIn(0, 1)
        validateSerial(BSBurstSlider::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::direction).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::tailBeat).correctType().correctType().existsBefore(ver, Schema3_3).optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validateSerial(BSBurstSlider::tailX).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::tailY).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::sliceCount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBurstSlider::squishAmount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::waypoints).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSWaypoint::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSWaypoint::x).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSWaypoint::y).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSWaypoint::offsetDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::basicBeatmapEvents).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSEventV3::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSEventV3::eventType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSEventV3::value).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSEventV3::floatValue).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::colorBoostBeatmapEvents).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSBoostEvent::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSBoostEvent::boost).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validateSerial(BSDifficultyV3::lightColorEventBoxGroups).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSLightColorEventBoxGroup::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightColorEventBoxGroup::groupId).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightColorEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSLightColorEventBox::indexFilter, ver)
            validateSerial(BSLightColorEventBox::beatDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightColorEventBox::beatDistributionParamType).correctType().existsBefore(ver, Schema3_3).isIn(1, 2)
            validateSerial(BSLightColorEventBox::brightnessDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightColorEventBox::brightnessDistributionParamType).correctType().existsBefore(ver, Schema3_3).isIn(1, 2)
            validateSerial(BSLightColorEventBox::brightnessDistributionShouldAffectFirstBaseEvent).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightColorEventBox::lightColorBaseDataList).correctType().optionalNotNull().validateForEach {
                validateSerial(BSLightColorBaseData::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(BSLightColorBaseData::transitionType).correctType().existsBefore(ver, Schema3_3).isIn(0, 1, 2)
                validateSerial(BSLightColorBaseData::colorType).correctType().existsBefore(ver, Schema3_3).isIn(-1, 0, 1, 2)
                validateSerial(BSLightColorBaseData::brightness).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(BSLightColorBaseData::strobeFrequency).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validateSerial(BSDifficultyV3::lightRotationEventBoxGroups).correctType().exists().optionalNotNull().validateForEach {
        validateSerial(BSLightRotationEventBoxGroup::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightRotationEventBoxGroup::groupId).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightRotationEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSLightRotationEventBox::indexFilter, ver)
            validateSerial(BSLightRotationEventBox::beatDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightRotationEventBox::beatDistributionParamType).correctType().existsBefore(ver, Schema3_3).isIn(1, 2)
            validateSerial(BSLightRotationEventBox::rotationDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightRotationEventBox::rotationDistributionParamType).correctType().existsBefore(ver, Schema3_3).isIn(1, 2)
            validateSerial(BSLightRotationEventBox::axis).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightRotationEventBox::flipRotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightRotationEventBox::brightnessDistributionShouldAffectFirstBaseEvent).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightRotationEventBox::lightRotationBaseDataList).correctType().optionalNotNull().validateForEach {
                validateSerial(LightRotationBaseData::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightRotationBaseData::usePreviousEventRotationValue).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightRotationBaseData::easeType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightRotationBaseData::loopsCount).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightRotationBaseData::rotation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightRotationBaseData::rotationDirection).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validateSerial(BSDifficultyV3::lightTranslationEventBoxGroups).correctType().optionalNotNull().onlyExistsAfter(ver, Schema3_2).validateForEach {
        validateSerial(BSLightTranslationEventBoxGroup::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightTranslationEventBoxGroup::groupId).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSLightTranslationEventBoxGroup::eventBoxes).correctType().existsBefore(ver, Schema3_3).optionalNotNull().validateForEach {
            validateEventBox(BSLightTranslationEventBox::indexFilter, ver)
            validateSerial(BSLightTranslationEventBox::gapDistributionParam).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightTranslationEventBox::gapDistributionParamType).correctType().existsBefore(ver, Schema3_3).isIn(1, 2)
            validateSerial(BSLightTranslationEventBox::axis).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightTranslationEventBox::flipTranslation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightTranslationEventBox::gapDistributionShouldAffectFirstBaseEvent).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightTranslationEventBox::gapDistributionEaseType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            validateSerial(BSLightTranslationEventBox::lightTranslationBaseDataList).correctType().existsBefore(ver, Schema3_3).optionalNotNull().validateForEach {
                validateSerial(LightTranslationBaseData::beat).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightTranslationBaseData::usePreviousEventTranslationValue).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightTranslationBaseData::easeType).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
                validateSerial(LightTranslationBaseData::translation).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validateSerial(BSDifficultyV3::vfxEventBoxGroups).correctType().optionalNotNull().onlyExistsAfter(ver, Schema3_3).validateForEach {
        validateSerial(BSVfxEventBoxGroup::beat).correctType().optionalNotNull()
        validateSerial(BSVfxEventBoxGroup::groupId).correctType().optionalNotNull()
        validateSerial(BSVfxEventBoxGroup::type).correctType().isIn(0, 1, 2)
        validateSerial(BSVfxEventBoxGroup::eventBoxes).correctType().optionalNotNull().validateForEach {
            validateEventBox(BSVfxEventBox::indexFilter, ver)
            validateSerial(BSVfxEventBox::beatDistributionParam).correctType().optionalNotNull()
            validateSerial(BSVfxEventBox::beatDistributionParamType).correctType().isIn(1, 2)
            validateSerial(BSVfxEventBox::vfxDistributionParam).correctType().optionalNotNull()
            validateSerial(BSVfxEventBox::vfxDistributionParamType).correctType().isIn(1, 2)
            validateSerial(BSVfxEventBox::vfxDistributionEaseType).correctType().optionalNotNull()
            validateSerial(BSVfxEventBox::vfxDistributionShouldAffectFirstBaseEvent).correctType().optionalNotNull()
            validateSerial(BSVfxEventBox::vfxBaseDataList).correctType().optionalNotNull().validateEach()
        }
    }
    validateSerial(BSDifficultyV3::_fxEventsCollection).correctType().optionalNotNull().onlyExistsAfter(ver, Schema3_3).validateOptional {
        validateSerial(BSFxEventsCollection::intEventsList).correctType().exists().optionalNotNull().validateForEach {
            validateSerial(BSIntFxEventBaseData::beat).correctType().optionalNotNull()
            validateSerial(BSIntFxEventBaseData::usePreviousEventValue).correctType().optionalNotNull()
            validateSerial(BSIntFxEventBaseData::value).correctType().optionalNotNull()
        }
        validateSerial(BSFxEventsCollection::floatEventsList).correctType().exists().optionalNotNull().validateForEach {
            validateSerial(BSFloatFxEventBaseData::beat).correctType().optionalNotNull()
            validateSerial(BSFloatFxEventBaseData::usePreviousEventValue).correctType().optionalNotNull()
            validateSerial(BSFloatFxEventBaseData::value).correctType().optionalNotNull()
            validateSerial(BSFloatFxEventBaseData::easeType).correctType().optionalNotNull()
        }
    }
    validateSerial(BSDifficultyV3::basicEventTypesWithKeywords).correctType().exists().optionalNotNull()
    validateSerial(BSDifficultyV3::useNormalEventsAsCompatibleEvents).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
}

fun <T : GroupableEventBox> Validator<T>.validateEventBox(indexFilter: KProperty1<T, OptionalProperty<BSIndexFilter?>>, ver: Version) {
    validateSerial(indexFilter).correctType().optionalNotNull().exists().validateOptional {
        validateSerial(BSIndexFilter::type).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSIndexFilter::param0).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSIndexFilter::param1).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        validateSerial(BSIndexFilter::reversed).correctType().existsBefore(ver, Schema3_3).optionalNotNull()
        listOf(
            BSIndexFilter::chunks,
            BSIndexFilter::randomType,
            BSIndexFilter::seed,
            BSIndexFilter::limit,
            BSIndexFilter::alsoAffectsType
        ).forEach { validateSerial(it).correctType().notExistsBefore(ver, Schema3_1).existsBetween(ver, Schema3_1, Schema3_3).optionalNotNull() }
    }
}
