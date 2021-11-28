package io.beatmaps.common.beatsaber

interface BSCustomData {
    val _customData: Any?

    fun getCustomData() = when (_customData) {
        is Map<*, *> -> _customData
        else -> mapOf<String, String>()
    } as Map<*, *>
}

data class BSDifficulty(
    val _version: String?,
    val _notes: List<BSNote> = listOf(),
    val _obstacles: List<BSObstacle> = listOf(),
    val _events: List<BSEvent> = listOf(),
    val _waypoints: List<Any> = listOf(),
    val _specialEventsKeywordFilters: Any?,
    override val _customData: Any?,
    val _BPMChanges: List<Any>?
) : BSCustomData

data class BSNote(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _lineLayer: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _cutDirection: Int = Int.MIN_VALUE,
    override val _customData: Any?
) : BSCustomData

data class BSObstacle(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _lineIndex: Int = Int.MIN_VALUE,
    val _type: Int = Int.MIN_VALUE,
    val _duration: Long = Long.MIN_VALUE,
    val _width: Int = Int.MIN_VALUE,
    override val _customData: Any?
) : BSCustomData

data class BSEvent(
    val _time: Float = Float.NEGATIVE_INFINITY,
    val _type: Int = Int.MIN_VALUE,
    val _value: Int = Int.MIN_VALUE,
    override val _customData: Any?
) : BSCustomData
