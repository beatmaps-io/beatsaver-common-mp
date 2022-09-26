interface SongLengthInfo {
    fun maximumBeat(bpm: Float): Float
    fun timeToSeconds(time: Float): Float
    fun secondsToTime(sec: Float): Float
}