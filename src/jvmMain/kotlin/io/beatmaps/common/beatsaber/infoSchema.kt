@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.AdditionalProperties
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.api.searchEnum
import io.beatmaps.common.copyTo
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.or
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
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
import org.valiktor.Validator
import org.valiktor.constraints.In
import org.valiktor.functions.validate
import org.valiktor.validate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Integer.max
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem

abstract class JAdditionalProperties : AdditionalProperties {
    override val properties = javaClass.declaredFields.filter { it.type == OptionalProperty::class.java }.map { it.name }.toSet()
}

data class ImageInfo(val format: String, val width: Int, val height: Int)

@Serializable
data class MapInfo(
    val _version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _songName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _songSubName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _songAuthorName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _levelAuthorName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _beatsPerMinute: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _songTimeOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _shuffle: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _shufflePeriod: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _previewStartTime: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _previewDuration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _songFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _coverImageFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _environmentName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _allDirectionsEnvironmentName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _environmentNames: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val _colorSchemes: OptionalProperty<List<OptionalProperty<MapColorScheme?>>?> = OptionalProperty.NotPresent,
    val _customData: OptionalProperty<MapCustomData?> = OptionalProperty.NotPresent,
    val _difficultyBeatmapSets: OptionalProperty<List<OptionalProperty<DifficultyBeatmapSet?>>?> = OptionalProperty.NotPresent
) {
    fun imageInfo(path: IZipPath?, info: ExtractedInfo) = path?.inputStream().use { stream ->
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

    private fun audioValid(audio: File, info: ExtractedInfo) =
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

    private fun songLengthInfo(info: ExtractedInfo, getFile: (String) -> IZipPath?, constraintViolations: MutableSet<ConstraintViolation>) =
        getFile("BPMInfo.dat")?.inputStream()?.use { stream ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            stream.copyTo(byteArrayOutputStream, sizeLimit = 50 * 1024 * 1024)

            jsonIgnoreUnknown.decodeFromString<BPMInfo>(readFromBytes(byteArrayOutputStream.toByteArray())).also {
                try {
                    it.validate()
                } catch (e: ConstraintViolationException) {
                    constraintViolations += e.constraintViolations.map { cv ->
                        DefaultConstraintViolation(
                            "`BPMInfo.dat`.${cv.property}",
                            cv.value,
                            cv.constraint
                        )
                    }
                }
            }
        } ?: LegacySongLengthInfo(info)

    fun validate(files: Set<String>, info: ExtractedInfo, audio: File, getFile: (String) -> IZipPath?) = validate(this) {
        info.songLengthInfo = songLengthInfo(info, getFile, constraintViolations)
        val ver = Version(_version.orNull())

        validate(MapInfo::_version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(MapInfo::_songName).correctType().exists().optionalNotNull().isNotBlank().validate(MetadataLength) { op ->
            op == null || op.validate { (it?.length ?: 0) + (_levelAuthorName.orNull()?.length ?: 0) <= 100 }
        }
        validate(MapInfo::_songSubName).correctType().exists().optionalNotNull()
        validate(MapInfo::_songAuthorName).correctType().exists().optionalNotNull()
        validate(MapInfo::_levelAuthorName).correctType().exists().optionalNotNull()
        validate(MapInfo::_beatsPerMinute).correctType().exists().optionalNotNull().isBetween(10f, 1000f)
        validate(MapInfo::_songTimeOffset).correctType().exists().isZero()
        validate(MapInfo::_shuffle).correctType().exists().optionalNotNull()
        validate(MapInfo::_shufflePeriod).correctType().exists().optionalNotNull()
        validate(MapInfo::_previewStartTime).correctType().exists().isPositiveOrZero()
        validate(MapInfo::_previewDuration).correctType().exists().isPositiveOrZero()
        validate(MapInfo::_songFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .validate(AudioFormat) { it == null || audioValid(audio, info) }
        val imageInfo = _coverImageFilename.orNull()?.let { imageInfo(getFile(it), info) }
        validate(MapInfo::_coverImageFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .validate(ImageFormat) { imageInfo == null || arrayOf("jpeg", "jpg", "png").contains(imageInfo.format) }
            .validate(ImageSquare) { imageInfo == null || imageInfo.width == imageInfo.height }
            .validate(ImageSize) { imageInfo == null || imageInfo.width >= 256 && imageInfo.height >= 256 }
        validate(MapInfo::_customData).correctType().validate {
            extraFieldsViolation(
                constraintViolations,
                it.orNull()?.additionalInformation?.keys ?: setOf()
            )
        }
        validate(MapInfo::_environmentName).correctType().exists().optionalNotNull()
        validate(MapInfo::_allDirectionsEnvironmentName).correctType().exists().isIn("GlassDesertEnvironment")
        validate(MapInfo::_customData).correctType().optionalNotNull().validateOptional {
            it.validate(this, files)
        }
        validate(MapInfo::_difficultyBeatmapSets).correctType().exists().optionalNotNull().isNotEmpty().validateForEach { it.validate(this, files, getFile, info, ver) }

        // V2.1
        validate(MapInfo::_environmentNames).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1)
        validate(MapInfo::_colorSchemes).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1).validateForEach { it.validate(this) }
    }

    fun toJson() = (jsonIgnoreUnknown.encodeToString(this) + "\n")
        .replace(Regex("\\[\n +]"), "[]")
        .replace("\n", "\r\n")
}

@Serializable
data class MapCustomData(
    val _contributors: OptionalProperty<List<OptionalProperty<Contributor?>>?> = OptionalProperty.NotPresent,
    val _editors: OptionalProperty<MapEditors?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(validator: Validator<MapCustomData>, files: Set<String>) = validator.apply {
        validate(MapCustomData::_contributors).correctType().optionalNotNull().validateForEach {
            it.validate(this, files)
        }
        validate(MapCustomData::_editors).correctType().optionalNotNull().validateOptional {
            it.validate(this)
        }
    }
}

@Serializable
data class MapColorScheme(
    val useOverride: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,
    val colorScheme: OptionalProperty<ColorScheme?> = OptionalProperty.NotPresent
) {
    fun validate(validator: Validator<MapColorScheme>) = validator.apply {
        validate(MapColorScheme::useOverride).correctType().optionalNotNull()
        validate(MapColorScheme::colorScheme).correctType().optionalNotNull().validateOptional {
            it.validate(this)
        }
    }
}

@Serializable
data class ColorScheme(
    val colorSchemeId: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val saberAColor: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val saberBColor: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColor0: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColor1: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val obstaclesColor: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColor0Boost: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColor1Boost: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColorW: OptionalProperty<BSColor?> = OptionalProperty.NotPresent,
    val environmentColorWBoost: OptionalProperty<BSColor?> = OptionalProperty.NotPresent
) {
    fun validate(validator: Validator<ColorScheme>) = validator.apply {
        validate(ColorScheme::colorSchemeId).correctType().optionalNotNull()

        listOf(
            ColorScheme::saberAColor, ColorScheme::saberBColor, ColorScheme::environmentColor0, ColorScheme::environmentColor1, ColorScheme::obstaclesColor,
            ColorScheme::environmentColor0Boost, ColorScheme::environmentColor1Boost, ColorScheme::environmentColorW, ColorScheme::environmentColorWBoost
        ).forEach { prop ->
            validate(prop).correctType().optionalNotNull().validateOptional {
                it.validate(this)
            }
        }
    }
}

@Serializable
data class BSColor(
    val r: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val g: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val b: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val a: OptionalProperty<Float?> = OptionalProperty.NotPresent
) {
    fun validate(validator: Validator<BSColor>) = validator.apply {
        validate(BSColor::r).correctType().optionalNotNull()
        validate(BSColor::g).correctType().optionalNotNull()
        validate(BSColor::b).correctType().optionalNotNull()
        validate(BSColor::a).correctType().optionalNotNull()
    }
}

@Serializable
data class MapEditors(
    val _lastEditedBy: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val beatSage: OptionalProperty<MapEditorVersion?> = OptionalProperty.NotPresent,
    val MMA2: OptionalProperty<MapEditorVersion?> = OptionalProperty.NotPresent,
    val ChroMapper: OptionalProperty<MapEditorVersion?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(
        validator: Validator<MapEditors>
    ) = validator.apply {
        validate(MapEditors::_lastEditedBy).correctType().optionalNotNull()
        validate(MapEditors::beatSage).correctType().optionalNotNull()
        validate(MapEditors::MMA2).correctType().optionalNotNull()
        validate(MapEditors::ChroMapper).correctType().optionalNotNull()
    }
}

@Serializable
data class MapEditorVersion(
    val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(
        validator: Validator<MapEditorVersion>
    ) = validator.apply {
        validate(MapEditorVersion::version).correctType().optionalNotNull()
    }
}

@Serializable
data class Contributor(
    val _role: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _name: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _iconPath: OptionalProperty<String?> = OptionalProperty.NotPresent
) {
    fun validate(
        validator: Validator<Contributor>,
        files: Set<String>
    ) = validator.apply {
        validate(Contributor::_role).correctType().optionalNotNull()
        validate(Contributor::_name).correctType().optionalNotNull()
        validate(Contributor::_iconPath).correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
    }
}

@Serializable
data class DifficultyBeatmapSet(
    val _beatmapCharacteristicName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _difficultyBeatmaps: OptionalProperty<List<OptionalProperty<DifficultyBeatmap?>>?>,
    val _customData: OptionalProperty<DifficultyBeatmapSetCustomData?> = OptionalProperty.NotPresent
) {
    fun validate(validator: Validator<DifficultyBeatmapSet>, files: Set<String>, getFile: (String) -> IZipPath?, info: ExtractedInfo, ver: Version) = validator.apply {
        val allowedCharacteristics = mutableSetOf("Standard", "NoArrows", "OneSaber", "360Degree", "90Degree", "Lightshow", "Lawless")
        if (ver.minor > 0) allowedCharacteristics.add("Legacy")

        validate(DifficultyBeatmapSet::_beatmapCharacteristicName).exists().isIn(allowedCharacteristics)
        validate(DifficultyBeatmapSet::_difficultyBeatmaps).exists().optionalNotNull().isNotEmpty().validateForEach {
            it.validate(this, self(), files, getFile, info, ver)
        }
        validate(DifficultyBeatmapSet::_customData).correctType().optionalNotNull().validateOptional {
            it.validate(this, files)
        }
    }

    private fun self() = this

    fun enumValue() = searchEnum<ECharacteristic>(_beatmapCharacteristicName.or(""))
}

@Serializable
data class DifficultyBeatmap(
    val _difficulty: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _difficultyRank: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _beatmapFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _noteJumpMovementSpeed: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _noteJumpStartBeatOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _beatmapColorSchemeIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _environmentNameIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _customData: OptionalProperty<DifficultyBeatmapCustomData?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    private fun diffValid(
        parent: Validator<*>.Property<*>,
        path: IZipPath?,
        characteristic: DifficultyBeatmapSet,
        difficulty: DifficultyBeatmap,
        info: ExtractedInfo
    ) = path?.inputStream().use { stream ->
        val byteArrayOutputStream = ByteArrayOutputStream()
        stream?.copyTo(byteArrayOutputStream, sizeLimit = 50 * 1024 * 1024)
        val bytes = byteArrayOutputStream.toByteArray()

        info.md.write(bytes)
        val jsonElement = jsonIgnoreUnknown.parseToJsonElement(readFromBytes(bytes))
        val diff = if (jsonElement.jsonObject.containsKey("version")) {
            jsonIgnoreUnknown.decodeFromJsonElement<BSDifficultyV3>(jsonElement)
        } else {
            jsonIgnoreUnknown.decodeFromJsonElement<BSDifficulty>(jsonElement)
        }

        info.diffs.getOrPut(characteristic) {
            mutableMapOf()
        }[difficulty] = diff

        val maxBeat = info.songLengthInfo?.maximumBeat(info.mapInfo._beatsPerMinute.or(0f)) ?: 0f
        parent.addConstraintViolations(
            when (diff) {
                is BSDifficulty -> Validator(diff).apply { this.validate(info, maxBeat) }
                is BSDifficultyV3 -> Validator(diff).apply { this.validateV3(info, maxBeat, Version(diff.version.orNull())) }
            }.constraintViolations.map { constraint ->
                DefaultConstraintViolation(
                    property = "`${path?.fileName}`.${constraint.property}",
                    value = constraint.value,
                    constraint = constraint.constraint
                )
            }
        )
    }

    private fun self() = this

    fun validate(
        validator: Validator<DifficultyBeatmap>,
        characteristic: DifficultyBeatmapSet,
        files: Set<String>,
        getFile: (String) -> IZipPath?,
        info: ExtractedInfo,
        ver: Version
    ) = validator.apply {
        extraFieldsViolation(
            constraintViolations,
            additionalInformation.keys
        )

        val allowedDiffNames = EDifficulty.values().map { it.name }.toSet()
        validate(DifficultyBeatmap::_difficulty).exists()
            .validate(In(allowedDiffNames)) { it == null || it.validate { q -> allowedDiffNames.any { dn -> dn.equals(q, true) } } }
            .validate(UniqueDiff(_difficulty.orNull())) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != self() && it._difficulty == self()._difficulty
                } == false
            }
        validate(DifficultyBeatmap::_difficultyRank).exists().isIn(EDifficulty.values().map { it.idx })
            .validate(UniqueDiff(EDifficulty.fromInt(_difficultyRank.or(0))?.name ?: "Unknown")) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != self() && it._difficultyRank == self()._difficultyRank
                } == false
            }
        validate(DifficultyBeatmap::_beatmapFilename).exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .also {
                val filename = _beatmapFilename.orNull()
                if (filename != null && files.contains(filename.lowercase())) {
                    diffValid(it, getFile(filename), characteristic, self(), info)
                }
            }

        validate(DifficultyBeatmap::_customData).optionalNotNull().correctType().validateOptional {
            it.validate(this)
        }

        // V2.1
        validate(DifficultyBeatmap::_beatmapColorSchemeIdx).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo._colorSchemes.orNull()?.size ?: 0)).notExistsBefore(ver, Schema2_1)
        validate(DifficultyBeatmap::_environmentNameIdx).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo._environmentNames.orNull()?.size ?: 0)).notExistsBefore(ver, Schema2_1)
    }

    fun enumValue() = EDifficulty.fromInt(_difficultyRank.or(0)) ?: searchEnum(_difficulty.or(""))
}

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
            DefaultConstraintViolation(
                property = it,
                value = null,
                constraint = MisplacedCustomData
            )
        }
}

@Serializable
data class DifficultyBeatmapCustomData(
    val _difficultyLabel: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _editorOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _editorOldOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _warnings: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val _information: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val _suggestions: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val _requirements: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(
        validator: Validator<DifficultyBeatmapCustomData>
    ) = validator.apply {
        validate(DifficultyBeatmapCustomData::_difficultyLabel).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::_editorOffset).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::_editorOldOffset).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::_warnings).correctType().optionalNotNull().validateForEach {
            // Required
        }
        validate(DifficultyBeatmapCustomData::_information).correctType().optionalNotNull().validateForEach {
            // Required
        }
        validate(DifficultyBeatmapCustomData::_suggestions).correctType().optionalNotNull().validateForEach {
            // Required
        }
        validate(DifficultyBeatmapCustomData::_requirements).correctType().optionalNotNull().validateForEach {
            // Required
        }
    }
}

@Serializable
data class DifficultyBeatmapSetCustomData(
    val _characteristicLabel: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _characteristicIconImageFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(
        validator: Validator<DifficultyBeatmapSetCustomData>,
        files: Set<String>
    ) = validator.apply {
        validate(DifficultyBeatmapSetCustomData::_characteristicLabel).correctType().optionalNotNull()
        validate(DifficultyBeatmapSetCustomData::_characteristicIconImageFilename).correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
    }
}
