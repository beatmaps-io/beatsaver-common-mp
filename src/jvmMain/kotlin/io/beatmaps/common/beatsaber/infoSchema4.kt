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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.valiktor.ConstraintViolationException
import org.valiktor.constraints.In
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.lang.Integer.max

@Serializable
data class MapInfoV4(
    val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val song: OptionalProperty<SongInfo?> = OptionalProperty.NotPresent,
    val audio: OptionalProperty<AudioInfo?> = OptionalProperty.NotPresent,
    val songPreviewFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val coverImageFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentNames: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val colorSchemes: OptionalProperty<List<OptionalProperty<MapColorSchemeV4?>>?> = OptionalProperty.NotPresent,
    val difficultyBeatmaps: OptionalProperty<List<OptionalProperty<DifficultyBeatmapV4?>>?> = OptionalProperty.NotPresent,
    val customData: OptionalProperty<JsonObject?> = OptionalProperty.NotPresent
) : BaseMapInfo() {
    override val audioDataFilename = audio.orNull()?.audioDataFilename?.orNull() ?: "BPMInfo.dat"

    override fun validate(files: Set<String>, info: ExtractedInfo, audio: File, preview: File, getFile: (String) -> IZipPath?) = validate(this) {
        info.songLengthInfo = songLengthInfo(info, getFile, constraintViolations)
        // val ver = Version(version.orNull())

        validate(MapInfoV4::version).correctType().exists().optionalNotNull().matches(Regex("\\d+\\.\\d+\\.\\d+"))
        validate(MapInfoV4::song).correctType().exists().optionalNotNull().validate().validate(MetadataLength) { op ->
            op == null || op.validate { (getSongName()?.length ?: 0) + getLevelAuthorNamesString().length <= 100 }
        }
        validate(MapInfoV4::audio).correctType().exists().optionalNotNull().validateOptional {
            it.validate(this, files) {
                audioValid(audio, info)
            }
        }
        validate(MapInfoV4::songPreviewFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .validate(AudioFormat) { it == null || audioValid(preview) }

        val imageInfo = coverImageFilename.orNull()?.let { imageInfo(getFile(it), info) }
        validate(MapInfoV4::coverImageFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .validate(ImageFormat) {
                // Ignore if it will be picked up by another validation (null, not in files)
                it == null || it.validate { q -> q == null || !files.contains(q.lowercase()) } ||
                    arrayOf("jpeg", "jpg", "png").contains(imageInfo?.format)
            }
            .validate(ImageSquare) { imageInfo == null || imageInfo.width == imageInfo.height }
            .validate(ImageSize) { imageInfo == null || imageInfo.width >= 256 && imageInfo.height >= 256 }

        validate(MapInfoV4::environmentNames).correctType().exists().optionalNotNull()
        validate(MapInfoV4::colorSchemes).correctType().exists().optionalNotNull().validateEach()
        validate(MapInfoV4::customData).correctType().validateOptional {
            extraFieldsViolation(
                constraintViolations,
                it.keys
            )
        }

        validate(MapInfoV4::difficultyBeatmaps).correctType().exists().optionalNotNull().isNotEmpty().validateForEach {
            it.validate(this, files, getFile, this@MapInfoV4, info)
        }
    }
    override fun getColorSchemes() = colorSchemes.orEmpty()
    override fun getEnvironments() = environmentNames.orEmpty()
    override fun getBpm() = audio.orNull()?.bpm?.orNull()
    override fun getSongName() = song.orNull()?.title?.orNull()
    override fun getSubName() = song.orNull()?.subTitle?.orNull()
    override fun getLevelAuthorNames() = difficultyBeatmaps.orNull()?.flatMap {
        it.orNull()?.beatmapAuthors?.orNull().let { authors ->
            authors?.mappers.orEmpty() + authors?.lighters.orEmpty()
        }
    }?.toSet() ?: setOf()
    override fun getSongAuthorName() = song.orNull()?.author?.orNull()
    override fun getSongFilename() = audio.orNull()?.songFilename?.orNull()
    override fun updateFiles(changes: Map<String, String>) =
        copy(
            audio = OptionalProperty.Present(
                audio.or(AudioInfo()).let {
                    it.copy(songFilename = it.songFilename.mapChanged(changes))
                }
            ),
            songPreviewFilename = songPreviewFilename.mapChanged(changes)
        )

    override fun getExtraFiles() =
        (songFiles() + beatmapExtraFiles() + audioDataFilename).toSet()

    private fun songFiles() =
        listOfNotNull(coverImageFilename.orNull(), getSongFilename(), songPreviewFilename.orNull())

    private fun beatmapExtraFiles() =
        difficultyBeatmaps.orEmpty().flatMap { diff ->
            diff.extraFiles()
        }

    override fun getPreviewInfo() = audio.orNull().let { a ->
        PreviewInfo(songPreviewFilename.or(""), a?.previewStartTime.or(0f), a?.previewDuration.or(0f))
    }

    override fun toJsonElement() = jsonIgnoreUnknown.encodeToJsonElement(this)
}

@Serializable
data class SongInfo(
    val title: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val subTitle: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val author: OptionalProperty<String?> = OptionalProperty.NotPresent
) : Validatable<SongInfo> {
    override fun validate(validator: BMValidator<SongInfo>) = validator.apply {
        validate(SongInfo::title).correctType().exists().optionalNotNull().isNotBlank()
        validate(SongInfo::subTitle).correctType().exists().optionalNotNull()
        validate(SongInfo::author).correctType().exists().optionalNotNull()
    }
}

@Serializable
data class AudioInfo(
    val songFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val songDuration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val audioDataFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val bpm: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val lufs: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val previewStartTime: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val previewDuration: OptionalProperty<Float?> = OptionalProperty.NotPresent
) {
    fun validate(validator: BMValidator<AudioInfo>, files: Set<String>, audioValid: (String?) -> Boolean) = validator.apply {
        validate(AudioInfo::songFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .validate(AudioFormat) { it == null || audioValid(it.orNull()) }
        validate(AudioInfo::songDuration).correctType().exists().optionalNotNull()
        validate(AudioInfo::audioDataFilename).correctType().exists().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
        validate(AudioInfo::bpm).correctType().exists().optionalNotNull().isBetween(10f, 1000f)
        validate(AudioInfo::lufs).correctType().exists().optionalNotNull()
        validate(AudioInfo::previewStartTime).correctType().exists().isPositiveOrZero()
        validate(AudioInfo::previewDuration).correctType().exists().isPositiveOrZero()
    }
}

@Serializable
data class MapColorSchemeV4(
    val useOverride: OptionalProperty<Boolean?> = OptionalProperty.NotPresent,
    val colorSchemeName: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val saberAColor: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val saberBColor: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColor0: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColor1: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val obstaclesColor: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColor0Boost: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColor1Boost: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColorW: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val environmentColorWBoost: OptionalProperty<String?> = OptionalProperty.NotPresent
) : Validatable<MapColorSchemeV4>, BaseColorScheme {
    override fun validate(validator: BMValidator<MapColorSchemeV4>) = validator.apply {
        validate(MapColorSchemeV4::useOverride).correctType().optionalNotNull()
        validate(MapColorSchemeV4::colorSchemeName).exists().correctType().optionalNotNull()

        listOf(
            MapColorSchemeV4::saberAColor, MapColorSchemeV4::saberBColor, MapColorSchemeV4::environmentColor0, MapColorSchemeV4::environmentColor1, MapColorSchemeV4::obstaclesColor,
            MapColorSchemeV4::environmentColor0Boost, MapColorSchemeV4::environmentColor1Boost, MapColorSchemeV4::environmentColorW, MapColorSchemeV4::environmentColorWBoost
        ).forEach { prop ->
            validate(prop).correctType().optionalNotNull().matches(regex)
        }
    }

    companion object {
        val regex = Regex("#?[0-9A-Fa-f]{8}")
    }
}

@Serializable
data class DifficultyBeatmapV4CustomData(
    override val difficultyLabel: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val warnings: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val information: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val suggestions: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val requirements: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : Validatable<DifficultyBeatmapV4CustomData>, DifficultyBeatmapCustomDataBase, JAdditionalProperties() {
    override fun validate(
        validator: BMValidator<DifficultyBeatmapV4CustomData>
    ) = validator.apply {
        validate(DifficultyBeatmapV4CustomData::difficultyLabel).correctType().optionalNotNull()
        validate(DifficultyBeatmapV4CustomData::warnings).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapV4CustomData::information).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapV4CustomData::suggestions).correctType().optionalNotNull().validateEach()
        validate(DifficultyBeatmapV4CustomData::requirements).correctType().optionalNotNull().validateEach()
    }
}

@Serializable
data class DifficultyBeatmapV4(
    val characteristic: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val difficulty: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val beatmapAuthors: OptionalProperty<BeatmapAuthors?> = OptionalProperty.NotPresent,
    val environmentNameIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val beatmapColorSchemeIdx: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    override val noteJumpMovementSpeed: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    override val noteJumpStartBeatOffset: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("beatmapDataFilename") @ValidationName("beatmapDataFilename")
    override val beatmapFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val lightshowDataFilename: OptionalProperty<String?> = OptionalProperty.NotPresent,
    override val customData: OptionalProperty<DifficultyBeatmapV4CustomData?> = OptionalProperty.NotPresent,
    override val additionalInformation: Map<String, JsonElement> = mapOf()
) : DifficultyBeatmapInfo, JAdditionalProperties() {
    private fun extractAndHash(stream: InputStream?, info: ExtractedInfo): JsonElement {
        val byteArrayOutputStream = ByteArrayOutputStream()
        stream?.copyTo(byteArrayOutputStream, sizeLimit = FileLimits.DIFF_LIMIT)
        val bytes = byteArrayOutputStream.toByteArray()

        info.toHash.write(bytes)
        return jsonIgnoreUnknown.parseToJsonElement(readFromBytes(bytes))
    }

    private fun <E, T> diffValid(
        parent: BMValidator<E>.BMProperty<T>,
        path: IZipPath?,
        info: ExtractedInfo
    ) = path?.inputStream().use { stream ->
        val jsonElement = extractAndHash(stream, info)

        try {
            val diff = BSDiff.parse(jsonElement).check()

            info.diffs.getOrPut(charEnum()) { mutableMapOf() }[this] = diff

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

    private fun <E, T> lightsValid(
        parent: BMValidator<E>.BMProperty<T>,
        path: IZipPath?,
        info: ExtractedInfo
    ) = path?.inputStream().use { stream ->
        val jsonElement = extractAndHash(stream, info)

        try {
            val lights = BSLights.parse(jsonElement).check()

            info.lights.getOrPut(charEnum()) { mutableMapOf() }[this] = lights

            val maxBeat = info.songLengthInfo?.maximumBeat(info.mapInfo.getBpm() ?: 0f) ?: 0f
            parent.addConstraintViolations(
                when (lights) {
                    is BSDifficulty -> BMValidator(lights).apply { this.validate(info, maxBeat) }
                    is BSDifficultyV3 -> BMValidator(lights).apply { this.validateV3(info, lights, maxBeat, Version(lights.version.orNull())) }
                    is BSLightingV4 -> BMValidator(lights).apply { this.validateV4(info, lights, maxBeat, Version(lights.version.orNull())) }
                }.constraintViolations.map { constraint ->
                    constraint.addParent(BMPropertyInfo("`${path?.fileName}`"))
                }
            )
        } catch (e: ConstraintViolationException) {
            parent.addConstraintViolations(e.constraintViolations.addParent(path?.fileName))
        }
    }

    fun validate(
        validator: BMValidator<DifficultyBeatmapV4>,
        files: Set<String>,
        getFile: (String) -> IZipPath?,
        mapInfo: MapInfoV4,
        info: ExtractedInfo
    ) = validator.apply {
        val self = this@DifficultyBeatmapV4

        extraFieldsViolation(
            constraintViolations,
            additionalInformation.keys
        )

        validate(DifficultyBeatmapV4::characteristic).exists().correctType().optionalNotNull()
        validate(DifficultyBeatmapV4::difficulty).exists().correctType().optionalNotNull()
            .validate(In(allowedDiffNames)) { it == null || it.validate { q -> allowedDiffNames.any { dn -> dn.equals(q, true) } } }
            .validate(UniqueDiff(difficulty.orNull())) {
                mapInfo.difficultyBeatmaps.orNull()?.mapNotNull { it.orNull() }?.any {
                    it != self && it.difficulty == self.difficulty && it.characteristic == self.characteristic
                } == false
            }
        validate(DifficultyBeatmapV4::beatmapAuthors).exists().correctType().optionalNotNull().validate()
        validate(DifficultyBeatmapV4::environmentNameIdx).exists().correctType().optionalNotNull()
            .isLessThan(max(1, mapInfo.getEnvironments().size))
        validate(DifficultyBeatmapV4::beatmapColorSchemeIdx).exists().correctType().optionalNotNull()
            .isLessThan(max(1, mapInfo.getColorSchemes().size))
        validate(DifficultyBeatmapV4::noteJumpMovementSpeed).exists().correctType().optionalNotNull()
        validate(DifficultyBeatmapV4::noteJumpStartBeatOffset).exists().correctType().optionalNotNull()

        validate(DifficultyBeatmapV4::beatmapFilename).exists().correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .also {
                val filename = beatmapFilename.orNull()
                if (filename != null && files.contains(filename.lowercase())) {
                    diffValid(it, getFile(filename), info)
                }
            }

        validate(DifficultyBeatmapV4::lightshowDataFilename).correctType().optionalNotNull()
            .validate(InFiles) { it == null || it.validate { q -> q == null || files.contains(q.lowercase()) } }
            .also {
                val filename = lightshowDataFilename.orNull()
                if (filename != null && files.contains(filename.lowercase())) {
                    lightsValid(it, getFile(filename), info)
                }
            }
    }

    override fun enumValue() = searchEnum<EDifficulty>(difficulty.or(""))
    override fun extraFiles() = setOfNotNull(beatmapFilename.orNull(), lightshowDataFilename.orNull())

    private fun charEnum() = searchEnum<ECharacteristic>(characteristic.or(""))

    companion object {
        val allowedDiffNames = EDifficulty.entries.map { it.name }.toSet()
    }
}

@Serializable
data class BeatmapAuthors(
    val mappers: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent,
    val lighters: OptionalProperty<List<OptionalProperty<String?>>?> = OptionalProperty.NotPresent
) : Validatable<BeatmapAuthors> {
    override fun validate(validator: BMValidator<BeatmapAuthors>) = validator.apply {
        validate(BeatmapAuthors::mappers).correctType().exists().optionalNotNull()
        validate(BeatmapAuthors::lighters).correctType().exists().optionalNotNull()
    }
}
