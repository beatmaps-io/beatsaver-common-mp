package io.beatmaps.common.util

import kotlin.math.max

object TextHelper {
    private const val NON_THIN: String = "[^iIl1\\.,']"

    private fun textWidth(str: String, monoWidth: Boolean) = if (monoWidth) {
        str.length
    } else {
        str.length - str.replace(NON_THIN.toRegex(), "").length / 2
    }

    private tailrec fun stepForward(text: String, end: Int, max: Int, monoWidth: Boolean): Int {
        val newEnd = text.indexOf(' ', end + 1).takeIf { it >= 0 } ?: text.length

        return if (textWidth(text.substring(0, newEnd) + "...", monoWidth) < max) {
            stepForward(text, newEnd, max, monoWidth)
        } else {
            end
        }
    }

    fun ellipsize(text: String, max: Int, monoWidth: Boolean = false) =
        if (textWidth(text, monoWidth) <= max) {
            text
        } else {
            val end = text.lastIndexOf(' ', max - 3)

            val newEnd = if (end == -1) {
                // Just one long word. Chop it off.
                max(0, max - 3)
            } else {
                stepForward(text, end, max, monoWidth)
            }

            "${text.take(newEnd)}..."
        }
}
