package io.beatmaps.common

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpHeaders
import io.ktor.http.content.LastModifiedVersion
import io.ktor.http.content.versions
import io.ktor.response.header
import io.ktor.response.respondFile
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.util.Date

private val illegalCharacters = arrayOf(
    '<', '>', ':', '/', '\\', '|', '?', '*', '"',
    '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007',
    '\u0008', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d', '\u000e', '\u000d',
    '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016',
    '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001f',
).toCharArray()

fun cleanString(str: String) = str.split(*illegalCharacters).joinToString()
fun downloadFilename(mapId: String, songName: String, levelAuthorName: String) =
    cleanString("$mapId ($songName - $levelAuthorName).zip")

data class CDNUpdate(
    val hash: String?,
    val mapId: Int,
    val published: Boolean?,
    val songName: String?,
    val levelAuthorName: String?,
    val deleted: Boolean
)
fun localAvatarFolder() = File(System.getenv("AVATAR_DIR") ?: "K:\\BMAvatar")
fun localFolder(hash: String) = File(System.getenv("ZIP_DIR") ?: "K:\\BeatSaver", hash.substring(0, 1))
fun localCoverFolder(hash: String) = File(System.getenv("COVER_DIR") ?: "K:\\BeatSaverCover", hash.substring(0, 1))
fun localAudioFolder(hash: String) = File(System.getenv("AUDIO_DIR") ?: "K:\\BeatSaverAudio", hash.substring(0, 1))
fun localPlaylistCoverFolder() = File(System.getenv("PLAYLIST_COVER_DIR") ?: "K:\\BeatSaverPlaylist")

suspend fun PipelineContext<*, ApplicationCall>.returnFile(file: File?, filename: String? = null) {
    if (file != null && file.exists()) {
        filename?.let {
            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"$it\""
            )
        }

        call.respondFile(file) {
            versions = versions.plus(LastModifiedVersion(Date(file.lastModified())))
        }
    } else {
        throw NotFoundException()
    }
}
