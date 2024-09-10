@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.FileLimits
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.valiktor.ConstraintViolationException
import org.valiktor.constraints.In
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Integer.max

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
) : BaseMapInfo() {
    override val audioDataFilename = "BPMInfo.dat"

    override fun validate(files: Set<String>, info: ExtractedInfo, audio: File, preview: File, getFile: (String) -> IZipPath?) = validate(this) {
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
            .validate(ImageFormat) {
                // Ignore if it will be picked up by another validation (null, not in files)
                it == null || it.validate { q -> q == null || !files.contains(q.lowercase()) } ||
                    arrayOf("jpeg", "jpg", "png").contains(imageInfo?.format)
            }
            .validate(ImageSquare) { imageInfo == null || imageInfo.width == imageInfo.height }
            .validate(ImageSize) { imageInfo == null || imageInfo.width >= 256 && imageInfo.height >= 256 }
        validate(MapInfo::_customData).correctType().optionalNotNull().validateOptional {
            extraFieldsViolation(
                constraintViolations,
                it.additionalInformation.keys
            )
            it.validate(this, files)
        }
        validate(MapInfo::_environmentName).correctType().exists().optionalNotNull()
        validate(MapInfo::_allDirectionsEnvironmentName).correctType().let { v ->
            val anyRotationDiffs = it._difficultyBeatmapSets.or(listOf()).any { set ->
                setOf("360Degree", "90Degree").contains(set.orNull()?._beatmapCharacteristicName?.orNull())
            }
            if (anyRotationDiffs) {
                v.isIn("GlassDesertEnvironment")
            } else {
                v.optionalNotNull()
            }
        }
        validate(MapInfo::_difficultyBeatmapSets).correctType().exists().optionalNotNull().isNotEmpty().validateForEach { it.validate(this, files, getFile, info, ver) }

        // V2.1
        validate(MapInfo::_environmentNames).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1)
        validate(MapInfo::_colorSchemes).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1).validateEach()
    }

    override fun getColorSchemes() = _colorSchemes.orEmpty()
    override fun getEnvironments() = _environmentNames.orEmpty()
    override fun getBpm() = _beatsPerMinute.orNull()
    override fun getSongName() = _songName.orNull()
    override fun getSubName() = _songSubName.orNull()
    override fun getLevelAuthorNames() = setOfNotNull(_levelAuthorName.orNull())
    override fun getSongAuthorName() = _songAuthorName.orNull()
    override fun getSongFilename() = _songFilename.orNull()
    override fun updateFiles(changes: Map<String, String>) = copy(_songFilename = _songFilename.mapChanged(changes))
    override fun getExtraFiles() =
        (songFiles() + contributorsExtraFiles() + beatmapExtraFiles()).toSet()

    private fun songFiles() =
        listOfNotNull(_coverImageFilename.orNull(), getSongFilename())

    private fun contributorsExtraFiles() =
        _customData.orNull()?._contributors.orEmpty().mapNotNull { it._iconPath.orNull() }

    private fun beatmapExtraFiles() =
        _difficultyBeatmapSets.orEmpty().flatMap { setNotNull ->
            listOfNotNull(setNotNull._customData.orNull()?._characteristicIconImageFilename?.orNull()).plus(
                setNotNull._difficultyBeatmaps.orEmpty().flatMap { it.extraFiles() }
            )
        }

    override fun toJsonElement() = jsonIgnoreUnknown.encodeToJsonElement(this)
    override fun getPreviewInfo() = PreviewInfo(_songFilename.or(""), _previewStartTime.or(0f), _previewDuration.or(0f))
}

@Serializable
data class MapCustomData(
    val _contributors: OptionalProperty<List<OptionalProperty<Contributor?>>?> = OptionalProperty.NotPresent,
    val _editors: OptionalProperty<MapEditors?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(validator: BMValidator<MapCustomData>, files: Set<String>) = validator.apply {
        validate(MapCustomData::_contributors).correctType().optionalNotNull().validateForEach {
            it.validate(this, files)
        }
        validate(MapCustomData::_editors).correctType().optionalNotNull().validate()
    }
}

@Serializable
data class MapColorScheme(
    val useOverride: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,
    val colorScheme: OptionalProperty<ColorScheme?> = OptionalProperty.NotPresent
) : BaseColorScheme, Validatable<MapColorScheme> {
    override fun validate(validator: BMValidator<MapColorScheme>) = validator.apply {
        validate(MapColorScheme::useOverride).correctType().optionalNotNull()
        validate(MapColorScheme::colorScheme).correctType().optionalNotNull().validate()
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
) : Validatable<ColorScheme> {
    override fun validate(validator: BMValidator<ColorScheme>) = validator.apply {
        validate(ColorScheme::colorSchemeId).correctType().optionalNotNull()

        listOf(
            ColorScheme::saberAColor, ColorScheme::saberBColor, ColorScheme::environmentColor0, ColorScheme::environmentColor1, ColorScheme::obstaclesColor,
            ColorScheme::environmentColor0Boost, ColorScheme::environmentColor1Boost, ColorScheme::environmentColorW, ColorScheme::environmentColorWBoost
        ).forEach { prop ->
            validate(prop).correctType().optionalNotNull().validate()
        }
    }
}

@Serializable
data class BSColor(
    val r: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val g: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val b: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val a: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : Validatable<BSColor> {
    override fun validate(validator: BMValidator<BSColor>) = validator.apply {
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
) : Validatable<MapEditors>, JAdditionalProperties() {
    override fun validate(
        validator: BMValidator<MapEditors>
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
) : Validatable<MapEditorVersion>, JAdditionalProperties() {
    override fun validate(
        validator: BMValidator<MapEditorVersion>
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
        validator: BMValidator<Contributor>,
        files: Set<String>
    ) = validator.apply {
        validate(Contributor::_role).correctType().optionalNotNull()
        validate(Contributor::_name).correctType().optionalNotNull()
        validate(Contributor::_iconPath).correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q.isNullOrEmpty() || files.contains(q.lowercase()) } }
    }
}

@Serializable
data class DifficultyBeatmapSet(
    val _beatmapCharacteristicName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _difficultyBeatmaps: OptionalProperty<List<OptionalProperty<DifficultyBeatmap?>>?>,
    val _customData: OptionalProperty<DifficultyBeatmapSetCustomData?> = OptionalProperty.NotPresent
) {
    fun validate(validator: BMValidator<DifficultyBeatmapSet>, files: Set<String>, getFile: (String) -> IZipPath?, info: ExtractedInfo, ver: Version) = validator.apply {
        val allowedCharacteristics = ECharacteristic.entries.let {
            if (ver < Schema2_1) it.minus(ECharacteristic.Legacy) else it
        }.map { it.name.removePrefix("_") }.toSet()

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

interface DifficultyBeatmapInfo : BSCustomData {
    fun enumValue(): EDifficulty
    fun extraFiles(): Set<String>
    override val customData: OptionalProperty<DifficultyBeatmapCustomDataBase?>
    val noteJumpMovementSpeed: OptionalProperty<Float?>
    val noteJumpStartBeatOffset: OptionalProperty<Float?>
    val beatmapFilename: OptionalProperty<String?>
}

interface DifficultyBeatmapCustomDataBase {
    val difficultyLabel: OptionalProperty<String?>
    val information: OptionalProperty<List<OptionalProperty<String?>>?>
    val warnings: OptionalProperty<List<OptionalProperty<String?>>?>
    val suggestions: OptionalProperty<List<OptionalProperty<String?>>?>
    val requirements: OptionalProperty<List<OptionalProperty<String?>>?>
}

@Serializable
data class DifficultyBeatmap(
    val _difficulty: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _difficultyRank: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_beatmapFilename") @ValidationName("_beatmapFilename")
    override val beatmapFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_noteJumpMovementSpeed")
    override val noteJumpMovementSpeed: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("_noteJumpStartBeatOffset")
    override val noteJumpStartBeatOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val _beatmapColorSchemeIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _environmentNameIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_customData") @ValidationName("_customData")
    override val customData: OptionalProperty<DifficultyBeatmapCustomData?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : DifficultyBeatmapInfo, JAdditionalProperties() {
    private fun <E, T> diffValid(
        parent: BMValidator<E>.BMProperty<T>,
        path: IZipPath?,
        characteristic: DifficultyBeatmapSet,
        info: ExtractedInfo
    ) = path?.inputStream().use { stream ->
        val byteArrayOutputStream = ByteArrayOutputStream()
        stream?.copyTo(byteArrayOutputStream, sizeLimit = FileLimits.DIFF_LIMIT)
        val bytes = byteArrayOutputStream.toByteArray()

        info.toHash.write(bytes)
        val jsonElement = jsonIgnoreUnknown.parseToJsonElement(readFromBytes(bytes))
        try {
            val diff = BSDiff.parse(jsonElement).check()

            info.diffs.getOrPut(characteristic.enumValue()) {
                mutableMapOf()
            }[this] = diff

            if (diff is BSLights) {
                info.lights.getOrPut(characteristic.enumValue()) {
                    mutableMapOf()
                }[this] = diff
            }

            val maxBeat = info.songLengthInfo?.maximumBeat(info.mapInfo.getBpm() ?: 0f) ?: 0f
            parent.addConstraintViolations(
                when (diff) {
                    is BSDifficulty -> BMValidator(diff).apply { this.validate(info, maxBeat) }
                    is BSDifficultyV3 -> BMValidator(diff).apply { this.validateV3(info, diff, maxBeat, Version(diff.version.orNull())) }
                    is BSDifficultyV4 -> BMValidator(diff).apply { this.validateV4(info, diff, maxBeat, Version(diff.version.orNull())) }
                }.constraintViolations.addParent(path?.fileName)
            )
        } catch (e: ConstraintViolationException) {
            parent.addConstraintViolations(e.constraintViolations.addParent(path?.fileName))
        }
    }

    fun validate(
        validator: BMValidator<DifficultyBeatmap>,
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

        val allowedDiffNames = EDifficulty.entries.map { it.name }.toSet()
        validate(DifficultyBeatmap::_difficulty).exists()
            .validate(In(allowedDiffNames)) { it == null || it.validate { q -> allowedDiffNames.any { dn -> dn.equals(q, true) } } }
            .validate(UniqueDiff(_difficulty.orNull())) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != this@DifficultyBeatmap && it._difficulty == this@DifficultyBeatmap._difficulty
                } == false
            }
        validate(DifficultyBeatmap::_difficultyRank).exists().isIn(EDifficulty.entries.map { it.idx })
            .validate(UniqueDiff(EDifficulty.fromInt(_difficultyRank.or(0))?.name ?: "Unknown")) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != this@DifficultyBeatmap && it._difficultyRank == this@DifficultyBeatmap._difficultyRank
                } == false
            }
        validate(DifficultyBeatmap::beatmapFilename).exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .also {
                val filename = beatmapFilename.orNull()
                if (filename != null && files.contains(filename.lowercase())) {
                    diffValid(it, getFile(filename), characteristic, info)
                }
            }

        validate(DifficultyBeatmap::customData).optionalNotNull().correctType().validate()

        // V2.1
        validate(DifficultyBeatmap::_beatmapColorSchemeIdx).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo.getColorSchemes().size)).notExistsBefore(ver, Schema2_1)
        validate(DifficultyBeatmap::_environmentNameIdx).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo.getEnvironments().size)).notExistsBefore(ver, Schema2_1)
    }

    override fun enumValue() = EDifficulty.fromInt(_difficultyRank.or(0)) ?: searchEnum(_difficulty.or(""))
    override fun extraFiles() = setOfNotNull(beatmapFilename.orNull())
}

@Serializable
data class DifficultyBeatmapCustomData(
    @SerialName("_difficultyLabel") @ValidationName("_difficultyLabel")
    override val difficultyLabel: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _editorOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val _editorOldOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_warnings") @ValidationName("_warnings")
    override val warnings: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    @SerialName("_information") @ValidationName("_information")
    override val information: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    @SerialName("_suggestions") @ValidationName("_suggestions")
    override val suggestions: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    @SerialName("_requirements") @ValidationName("_requirements")
    override val requirements: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : Validatable<DifficultyBeatmapCustomData>, DifficultyBeatmapCustomDataBase, JAdditionalProperties() {
    override fun validate(
        validator: BMValidator<DifficultyBeatmapCustomData>
    ) = validator.apply {
        validate(DifficultyBeatmapCustomData::difficultyLabel).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::_editorOffset).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::_editorOldOffset).correctType().optionalNotNull()
        validate(DifficultyBeatmapCustomData::warnings).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapCustomData::information).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapCustomData::suggestions).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapCustomData::requirements).correctType().optionalNotNull().validateEach()
    }
}

@Serializable
data class DifficultyBeatmapSetCustomData(
    val _characteristicLabel: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val _characteristicIconImageFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : JAdditionalProperties() {
    fun validate(
        validator: BMValidator<DifficultyBeatmapSetCustomData>,
        files: Set<String>
    ) = validator.apply {
        validate(DifficultyBeatmapSetCustomData::_characteristicLabel).correctType().optionalNotNull()
        validate(DifficultyBeatmapSetCustomData::_characteristicIconImageFilename).correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
    }
}
