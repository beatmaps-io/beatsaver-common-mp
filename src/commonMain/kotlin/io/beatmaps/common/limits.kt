package io.beatmaps.common

object FileLimits {
    private const val KB = 1024L
    private const val MB = 1024 * KB

    private const val KB_F = 1024f
    private const val MB_F = 1024 * KB_F

    private const val DEFAULT_LIMIT = 50 * MB

    const val DIFF_LIMIT = DEFAULT_LIMIT
    const val SONG_LIMIT = DEFAULT_LIMIT
    const val VIVIFY_LIMIT = DEFAULT_LIMIT
    const val PLAYLIST_IMAGE_LIMIT = 10 * MB

    fun printLimit(size: Long, limit: Long) = "${(size / MB_F).fixedStr(1)}/${(limit / MB_F).fixedStr(1)}MiB"
}
