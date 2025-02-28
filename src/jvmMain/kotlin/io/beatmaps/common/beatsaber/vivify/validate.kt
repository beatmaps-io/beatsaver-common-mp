package io.beatmaps.common.beatsaber.vivify

import io.beatmaps.common.FileLimits
import io.beatmaps.common.beatsaber.BMValidator
import io.beatmaps.common.beatsaber.custom.CustomJsonEvent
import io.beatmaps.common.beatsaber.info.MapInfo
import io.beatmaps.common.util.copyTo
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.kabt.file.UnityFileSystem
import io.beatmaps.kabt.tree.ComplexAsset
import io.beatmaps.kabt.tree.MapAsset
import io.beatmaps.kabt.tree.StringAsset
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.InputStream

object Vivify {
    private fun createTemp(stream: InputStream): File =
        File.createTempFile("asset", ".vivify").also { file ->
            file.deleteOnExit()

            file.outputStream().use {
                stream.copyTo(it, sizeLimit = FileLimits.VIVIFY_LIMIT)
            }
        }

    private fun getAssets(path: String, crc: UInt) =
        try {
            UnityFileSystem().use { ufs ->
                ufs.mount(path, "/").use { archive ->
                    VivifyFile(
                        true,
                        archive.crc32 == crc,
                        archive.nodes.filter { it.isSerializedFile }.mapNotNull { node ->
                            (node.reader.serializedFile.objects.firstOrNull { obj -> obj.typeId == 142 }?.asset as? ComplexAsset)?.let { rootAsset ->
                                val container = rootAsset.children.filterIsInstance<MapAsset>().firstOrNull { it.name == "m_Container" }
                                container?.map?.map { it.key }?.filterIsInstance<StringAsset>()?.map { it.string }
                            }
                        }.flatten().toSet()
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors
            VivifyFile.FAIL
        }

    data class VivifyFile(val parsed: Boolean, val crcMatch: Boolean, val assets: Set<String>, val compressedSize: Long = 0, val filename: String = "") {
        companion object {
            val FAIL = VivifyFile(parsed = false, crcMatch = true, assets = emptySet())
        }
    }

    val allowedBundles = setOf("_windows2019", "_windows2021", "_android2021")

    private fun getFileInfo(info: MapInfo) =
        info.customData.orNull()?._assetBundle?.orNull()?.map { abInfo ->
            val bundleKey = abInfo.key.removePrefix("_").replaceFirstChar { c -> c.uppercaseChar() }
            Triple(abInfo.key, "bundle$bundleKey.vivify", abInfo.value)
        } ?: emptyList()

    fun getFiles(info: MapInfo) = getFileInfo(info).map { it.second }

    private fun tryGetBundleFromZip(filename: String, crc: UInt, getFile: (String) -> IZipPath?) =
        getFile(filename)?.let { f ->
            val file = createTemp(f.inputStream())
            try {
                getAssets(file.path, crc).copy(filename = filename, compressedSize = f.compressedSize)
            } finally {
                file.delete()
            }
        }

    private const val TOTALBS_REPO = "https://repo.totalbs.dev/api/v1/bundle"

    @Serializable
    data class TotalBsRepoResponse(val hash: UInt, val downloadUrl: String, val createdDate: String)

    private suspend fun tryGetBundleFromServer(filename: String, crc: UInt, client: HttpClient) =
        try {
            val downloadUrl = client.get("$TOTALBS_REPO/$crc") { expectSuccess = true }.body<TotalBsRepoResponse>().downloadUrl
            val stream = client.get(downloadUrl) { expectSuccess = true }.bodyAsChannel().toInputStream()

            createTemp(stream).let { file ->
                try {
                    getAssets(file.path, crc).copy(filename = filename, compressedSize = 0)
                } finally {
                    file.delete()
                }
            }
        } catch (e: ResponseException) {
            null
        }

    suspend fun BMValidator<MapInfo>.BMProperty<*>.validateVivify(info: ExtractedInfo, getFile: (String) -> IZipPath?, client: HttpClient) {
        val vivifyFiles = getFileInfo(obj).map { (key, filename, crc) ->
            validate(VivifyName(key, allowedBundles)) {
                allowedBundles.contains(key)
            }

            tryGetBundleFromZip(filename, crc, getFile) ?: tryGetBundleFromServer(filename, crc, client) ?: VivifyFile.FAIL
        }

        val parsedFiles = vivifyFiles.filter { it.parsed }
        parsedFiles.firstNotNullOfOrNull { it.assets }?.let {
            info.vivifyAssets = it
        }
        info.vivifySize = vivifyFiles.sumOf { it.compressedSize }

        vivifyFiles.forEach { file ->
            validate(AssetsRead(file.filename)) {
                file.parsed
            }
            validate(VivifyCrc(file.filename)) {
                file.crcMatch
            }
            validate(VivifySize(file.filename, file.compressedSize, info.maxVivify)) {
                file.compressedSize < info.maxVivify
            }
        }
        validate(AssetsMatch) {
            parsedFiles.size <= 1 || parsedFiles.drop(1).all { it.assets == parsedFiles[0].assets }
        }
        validate(HasAssets) {
            parsedFiles.isEmpty() || parsedFiles[0].assets.size > 1
        }
    }

    private val typesWithAssets = setOf("SetMaterialProperty", "Blit", "InstantiatePrefab")
// private val typesWithoutAssets = setOf("SetGlobalProperty", "CreateCamera", "CreateScreenTexture", "DestroyObject", "SetAnimatorProperty", "SetRenderingSettings")

    fun <T : CustomJsonEvent> BMValidator<T>.BMProperty<JsonElement?>.validateVivify(assets: Set<String>) {
        val eventAssets = if (obj.type == "AssignObjectPrefab") {
            obj.data.jsonObject.filterKeys { it != "loadMode" }.flatMap {
                (if (it.key == "saber") setOf("trailAsset") else setOf("debrisAsset", "anyDirectionAsset")).plus("asset").mapNotNull { key ->
                    it.value.jsonObject[key]?.jsonPrimitive?.content
                }
            }
        } else {
            setOfNotNull(obj.data.jsonObject["asset"]?.jsonPrimitive?.content)
        }

        val hasAsset = typesWithAssets.contains(obj.type)
        eventAssets.forEach { asset ->
            validate(AssetExists(asset)) { !hasAsset || assets.contains(asset) }
        }
    }
}
