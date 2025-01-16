package io.beatmaps.common.beatsaber

actual class Version actual constructor(val version: String?) : Comparable<Version?> {
    private val numbers: IntArray by lazy {
        (version ?: "")
            .substringBefore('-')
            .split('.')
            .dropLastWhile { it.isEmpty() }
            .map(String::toInt)
            .toIntArray()
    }

    actual val major = numbers.getOrElse(0) { 0 }
    actual val minor = numbers.getOrElse(1) { 0 }
    actual val patch = numbers.getOrElse(2) { 0 }

    actual override fun compareTo(other: Version?): Int {
        if (other == null) return 1

        return numbers.zip(other.numbers)
            .firstOrNull { (left, right) -> left != right }?.let { (left, right) -> if (left < right) -1 else 1 } ?: 0
    }
}
