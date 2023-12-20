package io.beatmaps.common

import io.ktor.http.HttpHeaders
import io.ktor.http.content.versions
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.http.content.LastModifiedVersion
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URI
import java.net.URISyntaxException

private val illegalCharacters = arrayOf(
    '<', '>', ':', '/', '\\', '|', '?', '*', '"',
    '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007',
    '\u0008', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d', '\u000e', '\u000d',
    '\u000f', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016',
    '\u0017', '\u0018', '\u0019', '\u001a', '\u001b', '\u001c', '\u001d', '\u001f'
).toCharArray()

fun cleanString(str: String) = str.split(*illegalCharacters).joinToString("")
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

fun encodeURLPathComponent(path: String?): String =
    try {
        URI(null, null, path, null).toASCIIString()
    } catch (e: URISyntaxException) {
        ""
    }

suspend fun PipelineContext<*, ApplicationCall>.returnFile(file: File?, filename: String? = null) {
    if (file != null && file.exists()) {
        filename?.let {
            val encoded = encodeURLPathComponent(it)
            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"$it\"; filename*=utf-8''$encoded"
            )
        }

        call.respondFile(file) {
            versions = versions.plus(LastModifiedVersion(file.lastModified()))
        }
    } else {
        throw NotFoundException()
    }
}
