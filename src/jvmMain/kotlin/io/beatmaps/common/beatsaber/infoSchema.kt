@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.AdditionalProperties
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.copyTo
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
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
import org.valiktor.DefaultConstraintViolation
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem

abstract class JAdditionalProperties : AdditionalProperties {
    override val properties = javaClass.declaredFields.filter { it.type == OptionalProperty::class.java }.map { it.name }.toSet()
}

data class ImageInfo(val format: String, val width: Int, val height: Int)

abstract class BaseMapInfo {
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
        try {
            val header = OggFileReader().read(audio).audioHeader
            if (header is GenericAudioHeader) {
                info.duration = header.preciseLength
            } else {
                // May have been rounded down
                info.duration = (header.trackLength + 1).toFloat()
            }
        } catch (e: Exception) {
            try {
                AudioSystem.getAudioInputStream(audio).use { wavInfo ->
                    info.duration = ((wavInfo.frameLength + 0.0) / wavInfo.format.sampleRate).toFloat()
                }
            } catch (e: Exception) {
                null
            }
        } != null && info.duration > 0

    protected abstract val audioDataFilename: String
    protected fun songLengthInfo(info: ExtractedInfo, getFile: (String) -> IZipPath?, constraintViolations: MutableSet<ConstraintViolation>) =
        getFile(audioDataFilename)?.inputStream()?.use { stream ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            stream.copyTo(byteArrayOutputStream, sizeLimit = 50 * 1024 * 1024)

            jsonIgnoreUnknown.parseToJsonElement(readFromBytes(byteArrayOutputStream.toByteArray())).let { jsonElement ->
                BPMInfoBase.parse(jsonElement)
            }.also {
                try {
                    it.validate()
                } catch (e: ConstraintViolationException) {
                    constraintViolations += e.constraintViolations.map { cv ->
                        DefaultConstraintViolation(
                            "`$audioDataFilename`.${cv.property}",
                            cv.value,
                            cv.constraint
                        )
                    }
                }
            }
        } ?: LegacySongLengthInfo(info)

    abstract fun validate(files: Set<String>, info: ExtractedInfo, audio: File, getFile: (String) -> IZipPath?): BaseMapInfo

    abstract fun getColorSchemes(): List<BaseColorScheme>
    abstract fun getEnvironments(): List<String>
    abstract fun getBpm(): Float?
    abstract fun getSongName(): String?
    abstract fun getSubName(): String?
    abstract fun getLevelAuthorNames(): Set<String>
    abstract fun getSongAuthorName(): String?
    abstract fun getSongFilename(): String?
    abstract fun setSongFilename(filename: String?): BaseMapInfo

    abstract fun getExtraFiles(): Set<String>
    abstract fun toJsonElement(): JsonElement

    companion object {
        fun parse(element: JsonElement) =
            if (element.jsonObject.containsKey("version")) {
                jsonIgnoreUnknown.decodeFromJsonElement<MapInfoV4>(element)
            } else {
                jsonIgnoreUnknown.decodeFromJsonElement<MapInfo>(element)
            }
    }
}

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
