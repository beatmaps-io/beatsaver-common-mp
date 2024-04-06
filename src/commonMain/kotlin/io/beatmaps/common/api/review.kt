package io.beatmaps.common.api

enum class ReviewSentiment(val dbValue: Int, val emoji: String) {
    POSITIVE(1, "\uD83D\uDC9A"), NEGATIVE(-1, "\uD83D\uDC94"), NEUTRAL(0, "\uD83D\uDC9B");

    companion object {
        fun fromInt(x: Int) =
            when (x) {
                POSITIVE.dbValue -> POSITIVE
                NEGATIVE.dbValue -> NEGATIVE
                else -> NEUTRAL
            }
    }
}
