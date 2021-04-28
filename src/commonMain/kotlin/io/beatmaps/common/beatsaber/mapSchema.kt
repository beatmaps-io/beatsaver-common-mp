package io.beatmaps.common.beatsaber

data class BSDifficulty(
    val _version: String?,
    val _notes: List<BSNote> = listOf(),
    val _obstacles: List<BSObstacle> = listOf(),
    val _events: List<BSEvent> = listOf(),
    val _waypoints: List<Any> = listOf(),
    val _specialEventsKeywordFilters: Any?,
    val _customData: Any?,
    val _BPMChanges: List<Any>?
)

data class BSNote(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _lineLayer: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _cutDirection: Int = Int.MIN_VALUE,
    val _customData: Any?
)

data class BSObstacle(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _duration: Int = Int.MIN_VALUE,
    val _width: Int = Int.MIN_VALUE,
    val _customData: Any?
)

data class BSEvent(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _type: Int = Int.MIN_VALUE,
    val _value: Int = Int.MIN_VALUE,
    val _customData: Any?
)