package io.beatmaps.common.util

import kotlin.math.max

object TextHelper {
    private const val NON_THIN: String = "[^iIl1\\.,']"

    private fun textWidth(str: String): Int {
        return (str.length - str.replace(NON_THIN.toRegex(), "").length / 2)
    }

    private tailrec fun stepForward(text: String, end: Int, max: Int): Int {
        val newEnd = text.indexOf(' ', end + 1).takeIf { it >= 0 } ?: text.length

        return if (textWidth(text.substring(0, newEnd) + "...") < max) {
            stepForward(text, newEnd, max)
        } else {
            end
        }
    }

    fun ellipsize(text: String, max: Int) =
        if (textWidth(text) <= max) {
            text
        } else {
            val end = text.lastIndexOf(' ', max - 3)

            val newEnd = if (end == -1) {
                // Just one long word. Chop it off.
                max(0, max - 3)
            } else {
                stepForward(text, end, max)
            }

            "${text.take(newEnd)}..."
        }
}
