package io.beatmaps.common.beatsaber

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.api.searchEnum
import io.beatmaps.common.copyTo
import io.beatmaps.common.jackson
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.coobird.thumbnailator.Thumbnails
import org.jaudiotagger.audio.generic.GenericAudioHeader
import org.jaudiotagger.audio.ogg.OggFileReader
import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import org.valiktor.Validator
import org.valiktor.constraints.In
import org.valiktor.functions.isBetween
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isIn
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isNull
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.functions.isZero
import org.valiktor.functions.matches
import org.valiktor.functions.validate
import org.valiktor.functions.validateForEach
import org.valiktor.validate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Integer.max
import javax.imageio.ImageIO
import javax.sound.sampled.AudioSystem
import kotlin.reflect.KProperty1

data class MapInfo(
    val _version: String,
    val _songName: String,
    val _songSubName: String,
    val _songAuthorName: String,
    val _levelAuthorName: String,
    val _beatsPerMinute: Float,
    val _shuffle: Float,
    val _shufflePeriod: Float,
    val _previewStartTime: Float,
    val _previewDuration: Float,
    val _songFilename: String,
    val _coverImageFilename: String,
    val _environmentName: String,
    val _allDirectionsEnvironmentName: String?,
    val _environmentNames: List<String>?,
    val _colorSchemes: List<MapColorScheme>?,
    val _songTimeOffset: Float,
    val _customData: MapCustomData?,
    val _difficultyBeatmapSets: List<DifficultyBeatmapSet>
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

            jackson.readValue<BPMInfo>(byteArrayOutputStream.toByteArray()).also {
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
        val ver = Version(_version)

        validate(MapInfo::_version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(MapInfo::_songName).isNotNull().isNotBlank().validate(MetadataLength) {
            _songName.length + _levelAuthorName.length <= 100
        }
        validate(MapInfo::_beatsPerMinute).isNotNull().isBetween(10f, 1000f)
        validate(MapInfo::_previewStartTime).isPositiveOrZero()
        validate(MapInfo::_previewDuration).isPositiveOrZero()
        validate(MapInfo::_songFilename).isNotNull().validate(InFiles) { it == null || files.contains(it.lowercase()) }
            .validate(AudioFormat) { it == null || audioValid(audio, info) }
        val imageInfo = imageInfo(getFile(_coverImageFilename), info)
        validate(MapInfo::_coverImageFilename).isNotNull().validate(InFiles) { it == null || files.contains(it.lowercase()) }
            .validate(ImageFormat) { imageInfo != null && arrayOf("jpeg", "jpg", "png").contains(imageInfo.format) }
            .validate(ImageSquare) { imageInfo == null || imageInfo.width == imageInfo.height }
            .validate(ImageSize) { imageInfo == null || imageInfo.width >= 256 && imageInfo.height >= 256 }
        validate(MapInfo::_customData).validate {
            extraFieldsViolation(
                constraintViolations,
                it.additionalInformation.keys
            )
        }
        validate(MapInfo::_allDirectionsEnvironmentName).isIn("GlassDesertEnvironment")
        validate(MapInfo::_songTimeOffset).isZero()
        validate(MapInfo::_difficultyBeatmapSets).isNotNull().isNotEmpty().validateForEach { it.validate(this, files, getFile, info, ver) }

        // V2.1
        validate(MapInfo::_environmentNames).let { if (ver.minor == 0) it.isNull() }
        validate(MapInfo::_colorSchemes).let { cs -> if (ver.minor == 0) cs.isNull() else cs.validateForEach { it.validate(this) } }
    }
}

data class ImageInfo(val format: String, val width: Int, val height: Int)

object NodePresent : Constraint
object NodeNotPresent : Constraint

object InFiles : Constraint
object ImageSquare : Constraint
object ImageSize : Constraint
object ImageFormat : Constraint
object AudioFormat : Constraint
object CutDirection : Constraint
object MisplacedCustomData : Constraint
data class UniqueDiff(val diff: String) : Constraint
object MetadataLength : Constraint

val Schema3_1 = Version("3.1.0")
val Schema3_3 = Version("3.3.0")

data class MapCustomData(
    val _contributors: List<Contributor>?,
    val _editors: MapEditors?,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }
}

data class MapColorScheme(
    val useOverride: Boolean?,
    val colorScheme: ColorScheme?
) {
    fun validate(validator: Validator<MapColorScheme>) = validator.apply {
        validate(MapColorScheme::useOverride).isNotNull()
        validate(MapColorScheme::colorScheme).isNotNull()
    }
}

data class ColorScheme(
    val colorSchemeId: String?,
    val saberAColor: BSColor?,
    val saberBColor: BSColor?,
    val environmentColor0: BSColor?,
    val environmentColor1: BSColor?,
    val obstaclesColor: BSColor?,
    val environmentColor0Boost: BSColor?,
    val environmentColor1Boost: BSColor?,
    val environmentColorW: BSColor?,
    val environmentColorWBoost: BSColor?
)

data class BSColor(
    val r: Float?,
    val g: Float?,
    val b: Float?,
    val a: Float?
)

data class MapEditors(
    val _lastEditedBy: String?,
    val beatSage: MapEditorVersion?,
    @get:JsonProperty("MMA2")
    val MMA2: MapEditorVersion?,
    @get:JsonProperty("ChroMapper")
    val ChroMapper: MapEditorVersion?,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }
}

data class MapEditorVersion(
    val version: String,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }
}

data class Contributor(
    val _role: String? = null,
    val _name: String? = null,
    val _iconPath: String? = null
)

data class DifficultyBeatmapSet(
    val _beatmapCharacteristicName: String,
    val _difficultyBeatmaps: List<DifficultyBeatmap>,
    val _customData: DifficultyBeatmapSetCustomData?
) {
    fun validate(validator: Validator<DifficultyBeatmapSet>, files: Set<String>, getFile: (String) -> IZipPath?, info: ExtractedInfo, ver: Version) = validator.apply {
        val allowedCharacteristics = mutableSetOf("Standard", "NoArrows", "OneSaber", "360Degree", "90Degree", "Lightshow", "Lawless")
        if (ver.minor > 0) allowedCharacteristics.add("Legacy")

        validate(DifficultyBeatmapSet::_beatmapCharacteristicName).isNotNull().isIn(allowedCharacteristics)
        validate(DifficultyBeatmapSet::_difficultyBeatmaps).isNotNull().isNotEmpty().validateForEach {
            it.validate(this, self(), files, getFile, info, ver)
        }
    }

    private fun self() = this

    fun enumValue() = searchEnum<ECharacteristic>(_beatmapCharacteristicName)
}

data class DifficultyBeatmap(
    val _difficulty: String,
    val _difficultyRank: Int,
    val _beatmapFilename: String,
    val _noteJumpMovementSpeed: Float,
    val _noteJumpStartBeatOffset: Float,
    val _beatmapColorSchemeIdx: Int?,
    val _environmentNameIdx: Int?,
    val _customData: DifficultyBeatmapCustomData?,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }

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

        val maxBeat = info.songLengthInfo?.maximumBeat(info.mapInfo._beatsPerMinute) ?: 0f
        parent.addConstraintViolations(
            when (diff) {
                is BSDifficulty -> Validator(diff).apply { this.validate(info, maxBeat) }
                is BSDifficultyV3 -> Validator(diff).apply { this.validateV3(info, maxBeat, Version(diff.version)) }
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
            additionalInformation.keys,
            arrayOf("_warnings", "_information", "_suggestions", "_requirements", "_difficultyLabel", "_envColorLeft", "_envColorRight", "_colorLeft", "_colorRight")
        )

        val allowedDiffNames = EDifficulty.values().map { it.name }.toSet()
        validate(DifficultyBeatmap::_difficulty).isNotNull()
            .validate(In(allowedDiffNames)) { it == null || allowedDiffNames.any { dn -> dn.equals(it, true) } }
            .validate(UniqueDiff(_difficulty)) {
                !characteristic._difficultyBeatmaps.any {
                    it != self() && it._difficulty == self()._difficulty
                }
            }
        validate(DifficultyBeatmap::_difficultyRank).isNotNull().isIn(EDifficulty.values().map { it.idx })
            .validate(UniqueDiff(EDifficulty.fromInt(_difficultyRank)?.name ?: "Unknown")) {
                !characteristic._difficultyBeatmaps.any {
                    it != self() && it._difficultyRank == self()._difficultyRank
                }
            }
        validate(DifficultyBeatmap::_beatmapFilename).isNotNull().validate(InFiles) { it == null || files.contains(it.lowercase()) }
            .also {
                if (files.contains(_beatmapFilename.lowercase())) {
                    diffValid(it, getFile(_beatmapFilename), characteristic, self(), info)
                }
            }

        // V2.1
        validate(DifficultyBeatmap::_beatmapColorSchemeIdx).let {
            if (ver.minor > 0) it.isGreaterThanOrEqualTo(0).isLessThan(max(1, info.mapInfo._colorSchemes?.size ?: 0)) else it.isNull()
        }
        validate(DifficultyBeatmap::_environmentNameIdx).let {
            if (ver.minor > 0) it.isGreaterThanOrEqualTo(0).isLessThan(max(1, info.mapInfo._environmentNames?.size ?: 0)) else it.isNull()
        }
    }

    fun enumValue() = EDifficulty.fromInt(_difficultyRank) ?: searchEnum(_difficulty)
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

fun Validator<BSDifficulty>.validate(info: ExtractedInfo, maxBeat: Float) {
    validate(BSDifficulty::version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficulty::_notes).exists().optionalNotNull().validateForEach {
        validate(BSNote::_type).exists().isIn(0, 1, 3)
        validate(BSNote::_cutDirection).exists().optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNote::_time).exists().optionalNotNull().let {
            if (info.duration > 0) it.isBetween(0f, maxBeat)
        }
        validate(BSNote::_lineIndex).exists().optionalNotNull()
        validate(BSNote::_lineLayer).exists().optionalNotNull()
    }
    validate(BSDifficulty::_obstacles).exists().optionalNotNull().validateForEach {
        validate(BSObstacle::_type).exists().optionalNotNull()
        validate(BSObstacle::_duration).exists().optionalNotNull()
        validate(BSObstacle::_time).exists().optionalNotNull()
        validate(BSObstacle::_lineIndex).exists().optionalNotNull()
        validate(BSObstacle::_width).exists().optionalNotNull()
    }
    validate(BSDifficulty::_events).exists().optionalNotNull().validateForEach {
        validate(BSEvent::_time).exists().optionalNotNull()
        validate(BSEvent::_type).exists().optionalNotNull()
        validate(BSEvent::_value).exists().optionalNotNull()
    }
}

fun Validator<BSDifficultyV3>.validateV3(info: ExtractedInfo, maxBeat: Float, ver: Version) {
    validate(BSDifficultyV3::version).isNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
    validate(BSDifficultyV3::bpmEvents).exists().validateForEach {
        validate(BSBpmChange::bpm).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBpmChange::beat).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::rotationEvents).exists().validateForEach {
        validate(BSRotationEvent::executionTime).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSRotationEvent::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSRotationEvent::rotation).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorNotes).exists().validateForEach {
        validate(BSNoteV3::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSNoteV3::direction).existsBefore(ver, Schema3_3).optionalNotNull().validate(CutDirection) {
            it == null || it.validate { q ->
                q == null || (q in 0..8) || (q in 1000..1360)
            }
        }
        validate(BSNoteV3::_time).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }

        validate(BSNoteV3::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSNoteV3::angleOffset).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::bombNotes).exists().validateForEach {
        validate(BSBomb::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBomb::y).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::obstacles).exists().validateForEach {
        validate(BSObstacleV3::duration).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::width).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSObstacleV3::height).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::sliders).exists().validateForEach {
        validate(BSSlider::_time).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSSlider::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::direction).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailBeat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailX).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailY).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::headControlPointLengthMultiplier).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailControlPointLengthMultiplier).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::tailCutDirection).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSSlider::sliderMidAnchorMode).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::burstSliders).exists().validateForEach {
        validate(BSBurstSlider::_time).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }
        validate(BSBurstSlider::color).existsBefore(ver, Schema3_3).isIn(0, 1)
        validate(BSBurstSlider::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::direction).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailBeat).existsBefore(ver, Schema3_3).let {
            if (info.duration > 0) {
                it.isBetween(0f, maxBeat)
            } else {
                it.optionalNotNull()
            }
        }
        validate(BSBurstSlider::tailX).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::tailY).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::sliceCount).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBurstSlider::squishAmount).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::waypoints).exists().validateForEach {
        validate(BSWaypoint::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::x).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::y).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSWaypoint::offsetDirection).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::basicBeatmapEvents).exists().validateForEach {
        validate(BSEventV3::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::eventType).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::value).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSEventV3::floatValue).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::colorBoostBeatmapEvents).exists().validateForEach {
        validate(BSBoostEvent::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSBoostEvent::boost).existsBefore(ver, Schema3_3).optionalNotNull()
    }
    validate(BSDifficultyV3::lightColorEventBoxGroups).exists().validateForEach {
        validate(BSLightColorEventBoxGroup::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::groupId).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightColorEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSLightColorEventBox::indexFilter, ver)
            validate(BSLightColorEventBox::beatDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::beatDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::brightnessDistributionShouldAffectFirstBaseEvent).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightColorEventBox::lightColorBaseDataList).optionalNotNull().validateForEach {
                validate(BSLightColorBaseData::beat).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::transitionType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::colorType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::colorType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::brightness).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(BSLightColorBaseData::strobeFrequency).existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::lightRotationEventBoxGroups).exists().validateForEach {
        validate(BSLightRotationEventBoxGroup::beat).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::groupId).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSLightRotationEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSLightRotationEventBox::indexFilter, ver)
            validate(BSLightRotationEventBox::beatDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::beatDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::rotationDistributionParam).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::rotationDistributionParamType).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::axis).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::flipRotation).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::brightnessDistributionShouldAffectFirstBaseEvent).existsBefore(ver, Schema3_3).optionalNotNull()
            validate(BSLightRotationEventBox::lightRotationBaseDataList).isNotNull().validateForEach {
                validate(LightRotationBaseData::beat).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::usePreviousEventRotationValue).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::easeType).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::loopsCount).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotation).existsBefore(ver, Schema3_3).optionalNotNull()
                validate(LightRotationBaseData::rotationDirection).existsBefore(ver, Schema3_3).optionalNotNull()
            }
        }
    }
    validate(BSDifficultyV3::vfxEventBoxGroups).onlyExistsAfter(ver, Schema3_3).validateForEach {
        validate(BSVfxEventBoxGroup::beat).optionalNotNull()
        validate(BSVfxEventBoxGroup::groupId).optionalNotNull()
        validate(BSVfxEventBoxGroup::eventBoxes).optionalNotNull().validateForEach {
            validateEventBox(BSVfxEventBox::indexFilter, ver)
            validate(BSVfxEventBox::beatDistributionParam).optionalNotNull()
            validate(BSVfxEventBox::beatDistributionParamType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionParam).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionParamType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionEaseType).optionalNotNull()
            validate(BSVfxEventBox::vfxDistributionShouldAffectFirstBaseEvent).optionalNotNull()
            validate(BSVfxEventBox::vfxBaseDataList).optionalNotNull()
        }
    }
    validate(BSDifficultyV3::_fxEventsCollection).onlyExistsAfter(ver, Schema3_3).validateOptional {
        validate(BSFxEventsCollection::intEventsList).exists()
        validate(BSFxEventsCollection::floatEventsList).exists()
    }
    validate(BSDifficultyV3::basicEventTypesWithKeywords).existsBefore(ver, Schema3_3).optionalNotNull()
    validate(BSDifficultyV3::useNormalEventsAsCompatibleEvents).existsBefore(ver, Schema3_3).optionalNotNull()
}

fun <T : GroupableEventBox> Validator<T>.validateEventBox(indexFilter: KProperty1<T, OptionalProperty<BSIndexFilter?>>, ver: Version) {
    validate(indexFilter).exists().validateOptional {
        validate(BSIndexFilter::type).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param0).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::param1).existsBefore(ver, Schema3_3).optionalNotNull()
        validate(BSIndexFilter::reversed).existsBefore(ver, Schema3_3).optionalNotNull()
        listOf(
            BSIndexFilter::chunks,
            BSIndexFilter::randomType,
            BSIndexFilter::seed,
            BSIndexFilter::limit,
            BSIndexFilter::alsoAffectsType
        ).map { validate(it) }.forEach { it.notExistsBefore(ver, Schema3_1).existsBetween(ver, Schema3_1, Schema3_3).optionalNotNull() }
    }
}

data class DifficultyBeatmapCustomData(
    val _difficultyLabel: String?,
    val _editorOffset: Int?,
    val _editorOldOffset: Int?,
    val _warnings: List<String>?,
    val _information: List<String>?,
    val _suggestions: List<String>?,
    val _requirements: List<String>?,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }
}

data class DifficultyBeatmapSetCustomData(
    val _characteristicLabel: String?,
    val _characteristicIconImageFilename: String?,
    @JsonIgnore @get:JsonAnyGetter val additionalInformation: LinkedHashMap<String, Any> = linkedMapOf()
) {
    @JsonAnySetter
    fun ignored(name: String, value: Any) {
        additionalInformation[name] = value
    }
}
