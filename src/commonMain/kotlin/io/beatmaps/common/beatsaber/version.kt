package io.beatmaps.common.beatsaber

expect class Version(version: String?) : Comparable<Version?> {
    val major: Int
    val minor: Int
    val patch: Int
}