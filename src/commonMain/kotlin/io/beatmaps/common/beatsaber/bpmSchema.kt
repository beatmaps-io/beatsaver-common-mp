// ktlint-disable filename
package io.beatmaps.common.beatsaber

interface SongLengthInfo {
    fun maximumBeat(bpm: Float): Float
    fun timeToSeconds(time: Float): Float
    fun secondsToTime(sec: Float): Float
}
