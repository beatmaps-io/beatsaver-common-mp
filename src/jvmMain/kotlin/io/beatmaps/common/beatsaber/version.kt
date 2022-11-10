// ktlint-disable filename
package io.beatmaps.common.beatsaber

class Version(val version: String?) : Comparable<Version?> {
    private val numbers: IntArray by lazy {
        version!!
            .substringBefore('-')
            .split('.')
            .dropLastWhile { it.isEmpty() }
            .map(Integer::valueOf)
            .toIntArray()
    }

    val major = numbers.getOrElse(0) { 0 }
    val minor = numbers.getOrElse(1) { 0 }
    val patch = numbers.getOrElse(2) { 0 }

    override fun compareTo(other: Version?): Int {
        if (other == null) return 1

        return numbers.zip(other.numbers)
            .firstOrNull { (left, right) -> left != right }?.let { (left, right) -> if (left < right) -1 else 1 } ?: 0
    }
}
