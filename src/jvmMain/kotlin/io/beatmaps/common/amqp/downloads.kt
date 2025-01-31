package io.beatmaps.common.amqp

import kotlinx.serialization.Serializable

enum class DownloadType {
    HASH, KEY
}

@Serializable
data class DownloadInfo(val hash: String, val type: DownloadType, val remote: String)
