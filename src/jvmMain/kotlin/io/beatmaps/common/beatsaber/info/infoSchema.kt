@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.info

import io.beatmaps.common.AdditionalProperties
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.api.EBeatsaberEnvironment
import io.beatmaps.common.beatsaber.BMConstraintViolation
import io.beatmaps.common.beatsaber.BMPropertyInfo
import io.beatmaps.common.beatsaber.MisplacedCustomData
import io.beatmaps.common.beatsaber.MultipleVersionsConstraint
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.addParent
import io.beatmaps.common.beatsaber.custom.BSCustomData
import io.beatmaps.common.beatsaber.custom.InfoCustomData
import io.beatmaps.common.beatsaber.map.ParseResult
import io.beatmaps.common.beatsaber.map.parseBS
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.coobird.thumbnailator.Thumbnails
import org.jaudiotagger.audio.generic.GenericAudioHeader
import org.jaudiotagger.audio.ogg.OggFileReader
import org.valiktor.ConstraintViolation
import org.valiktor.ConstraintViolationException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem

abstract class JAdditionalProperties : AdditionalProperties {
    override val properties = javaClass.declaredFields.filter { it.type == OptionalProperty::class.java }.map { it.name }.toSet()
}

data class ImageInfo(val format: String, val width: Int, val height: Int)

fun <T> ParseResult<T>.check() = when (this) {
    is ParseResult.MultipleVersions -> {
        throw ConstraintViolationException(
            setOf(BMConstraintViolation(listOf(BMPropertyInfo("version/_version")), null, MultipleVersionsConstraint))
        )
    }
    is ParseResult.Success -> data
}

fun Iterable<ConstraintViolation>.addParent(fileName: String?) = this.map { constraint ->
    constraint.addParent(BMPropertyInfo("`$fileName`"))
}

abstract class BaseMapInfo : BSCustomData<InfoCustomData> {
    abstract val version: OptionalProperty<String?>

    protected fun imageInfo(path: IZipPath?, info: ExtractedInfo) = path?.inputStream().use { stream ->
        try {
            ImageIO.createImageInputStream(stream).use { iis ->
                val readers = ImageIO.getImageReaders(iis)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    val format = reader.formatName
                    reader.input = iis
                    val image = reader.read(0)

                    val newImageStream = ByteArrayOutputStream()
                    Thumbnails
                        .of(image)
                        .size(256, 256)
                        .imageType(BufferedImage.TYPE_INT_RGB)
                        .outputFormat("JPEG")
                        .outputQuality(0.8)
                        .toOutputStream(newImageStream)
                    info.thumbnail = newImageStream

                    ImageInfo(format.lowercase(), image.width, image.height)
                } else {
                    null
                }
            }
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: IOException) {
            null
        }
    }

    protected fun audioValid(audio: File, info: ExtractedInfo) =
        audioValid(audio) {
            info.duration = it
        }

    protected fun audioValid(audio: File, block: (Float) -> Unit = {}) =
        internalAudioValid(audio).let { duration ->
            if (duration != null) {
                block(duration)

                duration > 0
            } else {
                false
            }
        }

    private fun internalAudioValid(audio: File): Float? =
        try {
            val header = OggFileReader().read(audio).audioHeader
            if (header is GenericAudioHeader) {
                header.preciseLength
            } else {
                // May have been rounded down
                (header.trackLength + 1).toFloat()
            }
        } catch (e: Exception) {
            try {
                AudioSystem.getAudioInputStream(audio).use { wavInfo ->
                    ((wavInfo.frameLength + 0.0) / wavInfo.format.sampleRate).toFloat()
                }
            } catch (e: Exception) {
                null
            }
        }

    protected open fun songLengthInfo(info: ExtractedInfo, getFile: (String) -> IZipPath?, constraintViolations: MutableSet<ConstraintViolation>): SongLengthInfo =
        LegacySongLengthInfo(info)

    abstract fun validate(files: Set<String>, info: ExtractedInfo, audio: File, preview: File, maxVivify: Long, getFile: (String) -> IZipPath?): BaseMapInfo

    abstract fun getColorSchemes(): List<BaseColorScheme>
    abstract fun getEnvironments(): List<EBeatsaberEnvironment>
    protected abstract fun getEnvironment(rotation: Boolean): EBeatsaberEnvironment
    fun getEnvironment(index: Int?, rotation: Boolean = false) =
        index?.let { getEnvironments().getOrNull(index) } ?: getEnvironment(rotation)
    abstract fun getBpm(): Float?
    abstract fun getSongName(): String?
    abstract fun getSubName(): String?
    abstract fun getLevelAuthorNames(): Set<String>
    fun getLevelAuthorNamesString() = getLevelAuthorNames().joinToString()
    abstract fun getSongAuthorName(): String?
    abstract fun getSongFilename(): String?
    abstract fun updateFiles(changes: Map<String, String>): BaseMapInfo

    abstract fun getExtraFiles(): Set<String>
    abstract fun toJsonElement(): JsonElement
    abstract fun getPreviewInfo(): PreviewInfo

    companion object {
        fun parse(element: JsonElement) =
            element.jsonObject.parseBS({
                jsonIgnoreUnknown.decodeFromJsonElement<MapInfoV4>(element)
            }) {
                jsonIgnoreUnknown.decodeFromJsonElement<MapInfo>(element)
            }
    }
}

data class PreviewInfo(val filename: String, val start: Float, val duration: Float)

fun BaseMapInfo.toJson() = (jsonIgnoreUnknown.encodeToString(toJsonElement()) + "\n")
    .replace(Regex("\\[\n +]"), "[]")

interface BaseColorScheme

fun extraFieldsViolation(
    constraintViolations: MutableSet<ConstraintViolation>,
    keys: Set<String>,
    notAllowed: Array<String> = arrayOf(
        "_warnings",
        "_information",
        "_suggestions",
        "_requirements",
        "_difficultyLabel",
        "_envColorLeft",
        "_envColorRight",
        "_colorLeft",
        "_colorRight"
    )
) {
    constraintViolations +=
        notAllowed.intersect(keys).map {
            BMConstraintViolation(
                propertyInfo = listOf(BMPropertyInfo(it)),
                value = null,
                constraint = MisplacedCustomData
            )
        }
}
