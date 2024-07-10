package io.beatmaps.common

object FileLimits {
    private const val KB = 1024
    private const val MB = 1024 * KB

    private const val DEFAULT_LIMIT = 50 * MB

    const val DIFF_LIMIT = DEFAULT_LIMIT
    const val SONG_LIMIT = DEFAULT_LIMIT
    const val PLAYLIST_IMAGE_LIMIT = 10 * MB
}
