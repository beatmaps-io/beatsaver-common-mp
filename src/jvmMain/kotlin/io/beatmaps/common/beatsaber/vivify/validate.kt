package io.beatmaps.common.beatsaber.vivify

import io.beatmaps.common.FileLimits
import io.beatmaps.common.beatsaber.BMValidator
import io.beatmaps.common.beatsaber.custom.CustomJsonEvent
import io.beatmaps.common.beatsaber.info.MapInfo
import io.beatmaps.common.copyTo
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.kabt.file.UnityFileSystem
import io.beatmaps.kabt.tree.ComplexAsset
import io.beatmaps.kabt.tree.MapAsset
import io.beatmaps.kabt.tree.StringAsset
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

object Vivify {
    private fun createTemp(path: IZipPath): File =
        File.createTempFile("asset", ".vivify").also { file ->
            file.deleteOnExit()

            path.inputStream().use { iss ->
                file.outputStream().use {
                    iss.copyTo(it, sizeLimit = FileLimits.VIVIFY_LIMIT).second
                }
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

    fun BMValidator<MapInfo>.BMProperty<*>.validateVivify(info: ExtractedInfo, maxSize: Long, getFile: (String) -> IZipPath?) {
        val vivifyFiles = obj.customData.orNull()?._assetBundle?.orNull()?.map { abInfo ->
            validate(VivifyName(abInfo.key, allowedBundles)) {
                allowedBundles.contains(abInfo.key)
            }

            val bundleKey = abInfo.key.removePrefix("_").replaceFirstChar { c -> c.uppercaseChar() }
            val filename = "bundle$bundleKey.vivify"
            getFile(filename)?.let { f ->
                val file = createTemp(f)
                try {
                    getAssets(file.path, abInfo.value).copy(filename = filename, compressedSize = f.compressedSize)
                } finally {
                    file.delete()
                }
            } ?: VivifyFile.FAIL
        } ?: emptyList()

        val parsedFiles = vivifyFiles.filter { it.parsed }
        parsedFiles.firstNotNullOfOrNull { it.assets }?.let {
            info.vivifyAssets = it
        }
        info.vivifySize = vivifyFiles.sumOf { it.compressedSize }

        vivifyFiles.forEach { file ->
            validate(VivifyCrc(file.filename)) {
                file.crcMatch
            }
            validate(VivifySize(file.filename, file.compressedSize, maxSize)) {
                file.compressedSize < maxSize
            }
        }
        validate(AssetsRead) {
            vivifyFiles.all { it.parsed }
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
