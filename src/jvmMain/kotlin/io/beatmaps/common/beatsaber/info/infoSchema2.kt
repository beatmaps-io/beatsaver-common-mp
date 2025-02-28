@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.info

import io.beatmaps.common.FileLimits
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.api.EBeatsaberEnvironment
import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.AudioFormat
import io.beatmaps.common.beatsaber.BMConstraintViolation
import io.beatmaps.common.beatsaber.BMValidator
import io.beatmaps.common.beatsaber.ImageFormat
import io.beatmaps.common.beatsaber.ImageSize
import io.beatmaps.common.beatsaber.ImageSquare
import io.beatmaps.common.beatsaber.InFiles
import io.beatmaps.common.beatsaber.MetadataLength
import io.beatmaps.common.beatsaber.Schema2_1
import io.beatmaps.common.beatsaber.UniqueDiff
import io.beatmaps.common.beatsaber.Validatable
import io.beatmaps.common.beatsaber.Version
import io.beatmaps.common.beatsaber.correctType
import io.beatmaps.common.beatsaber.custom.BSCustomData
import io.beatmaps.common.beatsaber.custom.DifficultyBeatmapCustomDataBase
import io.beatmaps.common.beatsaber.custom.IContributor
import io.beatmaps.common.beatsaber.custom.InfoCustomData
import io.beatmaps.common.beatsaber.exists
import io.beatmaps.common.beatsaber.isBetween
import io.beatmaps.common.beatsaber.isGreaterThanOrEqualTo
import io.beatmaps.common.beatsaber.isIn
import io.beatmaps.common.beatsaber.isLessThan
import io.beatmaps.common.beatsaber.isNotBlank
import io.beatmaps.common.beatsaber.isNotEmpty
import io.beatmaps.common.beatsaber.isPositiveOrZero
import io.beatmaps.common.beatsaber.isZero
import io.beatmaps.common.beatsaber.map.BSDiff
import io.beatmaps.common.beatsaber.map.BSDifficulty
import io.beatmaps.common.beatsaber.map.BSDifficultyV3
import io.beatmaps.common.beatsaber.map.BSDifficultyV4
import io.beatmaps.common.beatsaber.map.BSLights
import io.beatmaps.common.beatsaber.map.ValidationName
import io.beatmaps.common.beatsaber.map.mapChanged
import io.beatmaps.common.beatsaber.map.orEmpty
import io.beatmaps.common.beatsaber.map.validate
import io.beatmaps.common.beatsaber.map.validateV3
import io.beatmaps.common.beatsaber.map.validateV4
import io.beatmaps.common.beatsaber.matches
import io.beatmaps.common.beatsaber.notExistsBefore
import io.beatmaps.common.beatsaber.optionalNotNull
import io.beatmaps.common.beatsaber.validate
import io.beatmaps.common.beatsaber.validateEach
import io.beatmaps.common.beatsaber.validateForEach
import io.beatmaps.common.beatsaber.validateOptional
import io.beatmaps.common.beatsaber.vivify.Vivify
import io.beatmaps.common.beatsaber.vivify.Vivify.validateVivify
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.or
import io.beatmaps.common.util.copyTo
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
import io.ktor.client.HttpClient
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
    @SerialName("_version") @ValidationName("_version")
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
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
    @SerialName("_customData") @ValidationName("_customData")
    override val customData: OptionalProperty<MapCustomData?> = OptionalProperty.NotPresent,
    val _difficultyBeatmapSets: OptionalProperty<List<OptionalProperty<DifficultyBeatmapSet?>>?> = OptionalProperty.NotPresent
) : BaseMapInfo() {
    override suspend fun validate(files: Set<String>, info: ExtractedInfo, audio: File, preview: File, client: HttpClient, getFile: (String) -> IZipPath?) = validate(this) {
        info.songLengthInfo = songLengthInfo(info, getFile, constraintViolations)
        val ver = Version(version.orNull())

        validate(MapInfo::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
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
            .validate(AudioFormat) { it == null || audioValid(audio, info) == AudioType.OGG }
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
        validate(MapInfo::customData).correctType().optionalNotNull().validateOptional {
            extraFieldsViolation(
                constraintViolations,
                it.additionalInformation.keys
            )
            it.validate(this, files)
        }
        validate(MapInfo::_environmentName).correctType().exists().isIn(EBeatsaberEnvironment.names)
        validate(MapInfo::_allDirectionsEnvironmentName).correctType().let { v ->
            val anyRotationDiffs = it._difficultyBeatmapSets.or(listOf()).any { set ->
                setOf("360Degree", "90Degree").contains(set.orNull()?._beatmapCharacteristicName?.orNull())
            }
            if (anyRotationDiffs) {
                v.isIn(EBeatsaberEnvironment.GlassDesertEnvironment.name)
            } else {
                v.optionalNotNull()
            }
        }
        // Must be validated before beatmaps so data is available
        validate(MapInfo::customData).validateVivify(info, getFile, client)
        validate(MapInfo::_difficultyBeatmapSets).correctType().exists().optionalNotNull().isNotEmpty().validateForEach { it.validate(this, files, getFile, info, ver) }

        // V2.1
        validate(MapInfo::_environmentNames).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1).validateForEach {
            if (!EBeatsaberEnvironment.names.contains(it)) {
                constraintViolations.add(
                    BMConstraintViolation(
                        propertyInfo = listOf(),
                        value = it,
                        constraint = In(EBeatsaberEnvironment.names)
                    )
                )
            }
        }
        validate(MapInfo::_colorSchemes).correctType().optionalNotNull().notExistsBefore(ver, Schema2_1).validateEach()
    }

    override fun getColorSchemes() = _colorSchemes.orEmpty()
    override fun getEnvironments() = _environmentNames.orEmpty().map {
        EBeatsaberEnvironment.fromString(it) ?: EBeatsaberEnvironment.DefaultEnvironment
    }
    override fun getEnvironment(rotation: Boolean) =
        if (rotation) { _allDirectionsEnvironmentName } else { _environmentName }
            .orNull()?.let { EBeatsaberEnvironment.fromString(it) } ?: EBeatsaberEnvironment.DefaultEnvironment

    override fun getBpm() = _beatsPerMinute.orNull()
    override fun getSongName() = _songName.orNull()
    override fun getSubName() = _songSubName.orNull()
    override fun getLevelAuthorNames() = setOfNotNull(_levelAuthorName.orNull())
    override fun getSongAuthorName() = _songAuthorName.orNull()
    override fun getSongFilename() = _songFilename.orNull()
    override fun updateFiles(changes: Map<String, String>) = copy(_songFilename = _songFilename.mapChanged(changes))
    override fun getExtraFiles() =
        (songFiles() + contributorsExtraFiles() + beatmapExtraFiles() + vivifyFiles()).toSet()

    private fun songFiles() =
        listOfNotNull(_coverImageFilename.orNull(), getSongFilename())

    private fun contributorsExtraFiles() =
        customData.orNull()?.contributors.orEmpty().mapNotNull { it.iconPath.orNull() }

    private fun beatmapExtraFiles() =
        _difficultyBeatmapSets.orEmpty().flatMap { setNotNull ->
            listOfNotNull(setNotNull._customData.orNull()?._characteristicIconImageFilename?.orNull()).plus(
                setNotNull._difficultyBeatmaps.orEmpty().flatMap { it.extraFiles() }
            )
        }

    private fun vivifyFiles() = Vivify.getFiles(this)

    override fun toJsonElement() = jsonIgnoreUnknown.encodeToJsonElement(this)
    override fun getPreviewInfo() = PreviewInfo(_songFilename.or(""), _previewStartTime.or(0f), _previewDuration.or(0f))
}

@Serializable
data class MapCustomData(
    @SerialName("_contributors") @ValidationName("_contributors")
    override val contributors: OptionalProperty<List<OptionalProperty<Contributor?>>?> = OptionalProperty.NotPresent,
    val _editors: OptionalProperty<MapEditors?> = OptionalProperty.NotPresent,
    val _assetBundle: OptionalProperty<Map<String, UInt>> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : InfoCustomData, JAdditionalProperties() {
    fun validate(validator: BMValidator<MapCustomData>, files: Set<String>) = validator.apply {
        validate(MapCustomData::contributors).correctType().optionalNotNull().validateForEach {
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
    @SerialName("_role") @ValidationName("_role")
    override val role: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_name") @ValidationName("_name")
    override val name: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_iconPath") @ValidationName("_iconPath")
    override val iconPath: OptionalProperty<String?> = OptionalProperty.NotPresent
) : IContributor {
    fun validate(
        validator: BMValidator<Contributor>,
        files: Set<String>
    ) = validator.apply {
        validate(Contributor::role).correctType().optionalNotNull()
        validate(Contributor::name).correctType().optionalNotNull()
        validate(Contributor::iconPath).correctType().optionalNotNull()
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
        }.map { it.human() }.toSet()

        validate(DifficultyBeatmapSet::_beatmapCharacteristicName).exists().isIn(allowedCharacteristics)
        validate(DifficultyBeatmapSet::_difficultyBeatmaps).exists().optionalNotNull().isNotEmpty().validateForEach {
            it.validate(this, self(), files, getFile, info, ver)
        }
        validate(DifficultyBeatmapSet::_customData).correctType().optionalNotNull().validateOptional {
            it.validate(this, files)
        }
    }

    private fun self() = this

    fun enumValue() = ECharacteristic.fromName(_beatmapCharacteristicName.or(""))
}

interface DifficultyBeatmapInfo : BSCustomData<DifficultyBeatmapCustomDataBase> {
    fun enumValue(): EDifficulty
    fun extraFiles(): Set<String>
    override val customData: OptionalProperty<DifficultyBeatmapCustomDataBase?>
    val noteJumpMovementSpeed: OptionalProperty<Float?>
    val noteJumpStartBeatOffset: OptionalProperty<Float?>
    val beatmapFilename: OptionalProperty<String?>
    val environmentIndex: OptionalProperty<Int?>
}

@Serializable
data class DifficultyBeatmap(
    @SerialName("_difficulty") @ValidationName("_difficulty")
    val difficulty: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_difficultyRank") @ValidationName("_difficultyRank")
    val difficultyRank: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_beatmapFilename") @ValidationName("_beatmapFilename")
    override val beatmapFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @SerialName("_noteJumpMovementSpeed") @ValidationName("_noteJumpMovementSpeed")
    override val noteJumpMovementSpeed: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("_noteJumpStartBeatOffset") @ValidationName("_noteJumpStartBeatOffset")
    override val noteJumpStartBeatOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("_beatmapColorSchemeIdx") @ValidationName("_beatmapColorSchemeIdx")
    val beatmapColorSchemeIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("_environmentNameIdx") @ValidationName("_environmentNameIdx")
    override val environmentIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
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
        validate(DifficultyBeatmap::difficulty).exists()
            .validate(In(allowedDiffNames)) { it == null || it.validate { q -> allowedDiffNames.any { dn -> dn.equals(q, true) } } }
            .validate(UniqueDiff(difficulty.orNull())) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != this@DifficultyBeatmap && it.difficulty == this@DifficultyBeatmap.difficulty
                } == false
            }
        validate(DifficultyBeatmap::difficultyRank).exists().isIn(EDifficulty.entries.map { it.idx })
            .validate(UniqueDiff(EDifficulty.fromInt(difficultyRank.or(0))?.name ?: "Unknown")) {
                characteristic._difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != this@DifficultyBeatmap && it.difficultyRank == this@DifficultyBeatmap.difficultyRank
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

        validate(DifficultyBeatmap::noteJumpMovementSpeed).correctType().optionalNotNull()
        validate(DifficultyBeatmap::noteJumpStartBeatOffset).correctType().optionalNotNull()
        validate(DifficultyBeatmap::customData).optionalNotNull().correctType().validate()

        // V2.1
        validate(DifficultyBeatmap::beatmapColorSchemeIdx).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo.getColorSchemes().size)).notExistsBefore(ver, Schema2_1)
        validate(DifficultyBeatmap::environmentIndex).optionalNotNull().isGreaterThanOrEqualTo(0)
            .isLessThan(max(1, info.mapInfo.getEnvironments().size)).notExistsBefore(ver, Schema2_1)
    }

    override fun enumValue() = EDifficulty.fromInt(difficultyRank.or(0)) ?: EDifficulty.fromName(difficulty.or(""))
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
