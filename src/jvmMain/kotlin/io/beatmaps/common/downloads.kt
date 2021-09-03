package io.beatmaps.common

enum class DownloadType {
    HASH, KEY
}
data class DownloadInfo(val hash: String, val type: DownloadType, val remote: String)
