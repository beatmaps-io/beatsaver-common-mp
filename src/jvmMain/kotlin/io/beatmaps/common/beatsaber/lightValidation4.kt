package io.beatmaps.common.beatsaber

import io.beatmaps.common.or
import io.beatmaps.common.zip.ExtractedInfo

fun BMValidator<BSLightingV4>.validateV4(info: ExtractedInfo, diff: BSLightingV4, maxBeat: Float, ver: Version) {
    validate(BSLightingV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSLightingV4::basicEvents).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSEventV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSEventV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::basicEventsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSEventDataV4::eventType).correctType().optionalNotNull()
        validate(BSEventDataV4::value).correctType().optionalNotNull()
        validate(BSEventDataV4::floatValue).correctType().optionalNotNull()
    }
    validate(BSLightingV4::colorBoostEvents).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSBoostEventV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBoostEventV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::colorBoostEventsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSBoostEventDataV4::boost).correctType().optionalNotNull()
    }
    validate(BSLightingV4::waypoints).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSWaypointV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSWaypointV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::waypointsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSWaypointDataV4::x).correctType().optionalNotNull()
        validate(BSWaypointDataV4::y).correctType().optionalNotNull()
        validate(BSWaypointDataV4::offsetDirection).correctType().optionalNotNull()
    }
    validate(BSLightingV4::basicEventTypesWithKeywords).correctType().exists().optionalNotNull()
    validate(BSLightingV4::eventBoxGroups).correctType().exists().optionalNotNull().validateForEach { group ->
        val beat = group.beat.or(0f)
        val type = group.enumValue()

        validate(BSEventBoxGroupV4::beat).correctType().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSEventBoxGroupV4::groupId).correctType().optionalNotNull()
        validate(BSEventBoxGroupV4::type).correctType().optionalNotNull()
        validate(BSEventBoxGroupV4::eventBoxes).correctType().exists().optionalNotNull().validateForEach { box ->
            validate(BSEventBoxV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
                box.getData(diff) != null
            }
            validate(BSEventBoxV4::eventBoxIndex).correctType().optionalNotNull().validate(IndexedConstraint) {
                type?.let { box.getEventBox(diff, it) } != null
            }
            validate(BSEventBoxV4::events).correctType().exists().optionalNotNull().validateForEach { event ->
                validate(BSEventBoxDataV4::beatOffset).correctType().optionalNotNull().let {
                    if (info.duration > 0) it.isBetween(-beat, maxBeat - beat)
                }
                validate(BSEventBoxDataV4::index).correctType().optionalNotNull().validate(IndexedConstraint) {
                    type?.let { event.getEvent(diff, type) } != null
                }
            }
        }
    }
    validate(BSLightingV4::indexFilters).correctType().exists().optionalNotNull().validateForEach {
        validate(BSIndexFilterV4::chunks).correctType().optionalNotNull()
        validate(BSIndexFilterV4::type).correctType().optionalNotNull()
        validate(BSIndexFilterV4::param0).correctType().optionalNotNull()
        validate(BSIndexFilterV4::param1).correctType().optionalNotNull()
        validate(BSIndexFilterV4::reversed).correctType().optionalNotNull()
        validate(BSIndexFilterV4::randomType).correctType().optionalNotNull()
        validate(BSIndexFilterV4::seed).correctType().optionalNotNull()
        validate(BSIndexFilterV4::limit).correctType().optionalNotNull()
        validate(BSIndexFilterV4::alsoAffectsType).correctType().optionalNotNull()
    }
    validate(BSLightingV4::lightColorEventBoxes).correctType().exists().optionalNotNull().validateForEach {
        validateBox()
    }
    validate(BSLightingV4::lightColorEvents).correctType().exists().optionalNotNull().validateForEach {
        validateEvent()
        validate(BSLightColorEventV4::colorType).correctType().optionalNotNull()
        validate(BSLightColorEventV4::strobeFrequency).correctType().optionalNotNull()
        validate(BSLightColorEventV4::strobeBrightness).correctType().optionalNotNull()
        validate(BSLightColorEventV4::strobeFade).correctType().optionalNotNull()
    }
    validate(BSLightingV4::lightRotationEventBoxes).correctType().exists().optionalNotNull().validateForEach {
        validateBox()
        validate(BSLightRotationEventBoxV4::invertAxis).correctType().optionalNotNull()
    }
    validate(BSLightingV4::lightRotationEvents).correctType().exists().optionalNotNull().validateForEach {
        validateEvent()
        validate(BSLightRotationEventV4::direction).correctType().optionalNotNull()
        validate(BSLightRotationEventV4::loopCount).correctType().optionalNotNull()
    }
    validate(BSLightingV4::lightTranslationEventBoxes).correctType().exists().optionalNotNull().validateForEach {
        validateBox()
        validate(BSLightTranslationEventBoxV4::axis).correctType().optionalNotNull()
        validate(BSLightTranslationEventBoxV4::invertAxis).correctType().optionalNotNull()
    }
    validate(BSLightingV4::lightTranslationEvents).correctType().exists().optionalNotNull().validateForEach {
        validateEvent()
    }
    validate(BSLightingV4::fxEventBoxes).correctType().exists().optionalNotNull().validateForEach {
        validateBox()
    }
    validate(BSLightingV4::floatFxEvents).correctType().exists().optionalNotNull().validateForEach {
        validateEvent()
    }
    validate(BSLightingV4::useNormalEventsAsCompatibleEvents).exists().correctType().optionalNotNull()
    validate(BSLightingV4::customData).correctType().optionalNotNull()
}

@Suppress("UNCHECKED_CAST")
fun <T : BoxedEvent> BMValidator<T>.validateEvent() {
    with(this as BMValidator<BoxedEvent>) {
        validate(BoxedEvent::transitionType).correctType().optionalNotNull()
        validate(BoxedEvent::easeType).correctType().optionalNotNull()
        validate(BoxedEvent::magnitude).correctType().optionalNotNull()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : EventBoxV4> BMValidator<T>.validateBox() {
    with(this as BMValidator<EventBoxV4>) {
        validate(EventBoxV4::beatDistributionParam).correctType().optionalNotNull()
        validate(EventBoxV4::beatDistributionParamType).correctType().optionalNotNull()
        validate(EventBoxV4::magnitudeDistributionParam).correctType().optionalNotNull()
        validate(EventBoxV4::magnitudeDistributionParamType).correctType().optionalNotNull()
        validate(EventBoxV4::distributionShouldAffectFirstBaseEvent).correctType().optionalNotNull()
        validate(EventBoxV4::easeType).correctType().optionalNotNull()
    }
}
