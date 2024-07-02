package io.beatmaps.common.beatsaber

import io.beatmaps.common.zip.ExtractedInfo

fun BMValidator<BSLightingV4>.validateV4(info: ExtractedInfo, diff: BSLightingV4, maxBeat: Float, ver: Version) {
    validate(BSLightingV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSLightingV4::basicEvents).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSEventV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSEventV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::basicEventsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSEventDataV4::eventType).correctType().exists().optionalNotNull()
        validate(BSEventDataV4::value).correctType().exists().optionalNotNull()
        validate(BSEventDataV4::floatValue).correctType().exists().optionalNotNull()
    }
    validate(BSLightingV4::colorBoostEvents).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSBoostEventV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSBoostEventV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::colorBoostEventsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSBoostEventDataV4::boost).correctType().exists().optionalNotNull()
    }
    validate(BSLightingV4::waypoints).correctType().exists().optionalNotNull().validateForEach { event ->
        validate(BSWaypointV4::beat).correctType().exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSWaypointV4::index).correctType().exists().optionalNotNull().validate(IndexedConstraint) {
            event.getData(diff) != null
        }
    }
    validate(BSLightingV4::waypointsData).correctType().exists().optionalNotNull().validateForEach {
        validate(BSWaypointDataV4::x).correctType().exists().optionalNotNull()
        validate(BSWaypointDataV4::y).correctType().exists().optionalNotNull()
        validate(BSWaypointDataV4::offsetDirection).correctType().exists().optionalNotNull()
    }
    validate(BSLightingV4::basicEventTypesWithKeywords).correctType().exists().optionalNotNull()
}
