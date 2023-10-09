package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.DifficultyBeatmap
import io.beatmaps.common.beatsaber.InFiles
import io.beatmaps.common.beatsaber.MapColorScheme
import io.beatmaps.common.beatsaber.MetadataLength
import io.beatmaps.common.beatsaber.MisplacedCustomData
import io.beatmaps.common.schema.SchemaCommon.infoPartialViolation
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.infoViolationWrong
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.DefaultConstraintViolation
import org.valiktor.constraints.Between
import org.valiktor.constraints.Equals
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.NotBlank
import org.valiktor.constraints.NotEmpty
import org.valiktor.constraints.NotNull
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoTest20 {
    @Test
    fun basic() {
        val ex = validateFolder("info/2_0/basic")
        assertNull(ex)
    }

    @Test
    fun additional() {
        val ex = validateFolder("info/2_0/additional")
        assertNull(ex)
    }

    @Test
    fun custom() {
        val ex = validateFolder("info/2_0/custom")
        assertNull(ex)
    }

    @Test
    fun `null`() {
        val ex = validateFolder("info/2_0/null")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_version", null, NotNull),
                infoViolation("_songName", null, NotNull),
                infoViolation("_songSubName", null, NotNull),
                infoViolation("_songAuthorName", null, NotNull),
                infoViolation("_levelAuthorName", null, NotNull),
                infoViolation("_beatsPerMinute", null, NotNull),
                infoViolation("_shuffle", null, NotNull),
                infoViolation("_shufflePeriod", null, NotNull),
                infoViolation("_songFilename", null, NotNull),
                infoViolation("_coverImageFilename", null, NotNull),
                infoViolation("_environmentName", null, NotNull),
                infoPartialViolation("_allDirectionsEnvironmentName", In::class),
                infoViolation("_customData._contributors[0]._role", null, NotNull),
                infoViolation("_customData._contributors[0]._name", null, NotNull),
                infoViolation("_customData._contributors[0]._iconPath", null, NotNull),
                infoViolation("_customData._contributors[1]", null, NotNull),
                infoViolation("_customData._editors._lastEditedBy", null, NotNull),
                infoPartialViolation("_difficultyBeatmapSets[0]._beatmapCharacteristicName", In::class),
                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficulty", In::class),
                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyRank", In::class),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapFilename", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._difficultyLabel", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._editorOffset", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._editorOldOffset", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._warnings[0]", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._information[0]", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._suggestions[0]", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._requirements[0]", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[1]", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._customData._characteristicLabel", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._customData._characteristicIconImageFilename", null, NotNull),
                infoViolation("_difficultyBeatmapSets[1]._difficultyBeatmaps", null, NotNull),
                infoViolation("_difficultyBeatmapSets[2]", null, NotNull)
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun null2() {
        val ex = validateFolder("info/2_0/null2")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_customData._contributors", null, NotNull),
                infoViolation("_customData._editors.MMA2", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._warnings", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._information", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._suggestions", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._requirements", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._customData", null, NotNull)
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun null3() {
        val ex = validateFolder("info/2_0/null3")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_customData._editors", null, NotNull),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData", null, NotNull)
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun null4() {
        val ex = validateFolder("info/2_0/null4")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_customData", null, NotNull),
                infoViolation("_difficultyBeatmapSets", null, NotNull)
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun error() {
        val ex = validateFolder("info/2_0/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_songName", "", NotBlank),
                infoViolation("_songName", "", MetadataLength),
                infoPartialViolation("_beatsPerMinute", Between::class),
                infoPartialViolation("_songTimeOffset", Equals::class),
                infoPartialViolation("_previewStartTime", GreaterOrEqual::class),
                infoPartialViolation("_previewDuration", GreaterOrEqual::class),
                infoViolation("_songFilename", "missing.ogg", InFiles),
                infoViolation("_coverImageFilename", "missing.png", InFiles),

                DefaultConstraintViolation("_customData._warnings", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._information", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._suggestions", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._requirements", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._difficultyLabel", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._envColorLeft", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._envColorRight", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._colorLeft", null, MisplacedCustomData),
                DefaultConstraintViolation("_customData._colorRight", null, MisplacedCustomData),

                infoPartialViolation("_allDirectionsEnvironmentName", In::class),

                infoPartialViolation("_difficultyBeatmapSets[0]._beatmapCharacteristicName", In::class),

                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._warnings", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._information", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._suggestions", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._requirements", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyLabel", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._envColorLeft", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._envColorRight", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._colorLeft", null, MisplacedCustomData),
                DefaultConstraintViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._colorRight", null, MisplacedCustomData),

                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficulty", In::class),
                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyRank", In::class),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapFilename", "missing.dat", InFiles),
                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx", Less::class),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx", 1),
                infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx", Less::class),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx", 1),

                infoPartialViolation("_difficultyBeatmapSets[1]._beatmapCharacteristicName", In::class),
                infoViolation("_difficultyBeatmapSets[1]._difficultyBeatmaps", listOf<OptionalProperty.Present<DifficultyBeatmap>>(), NotEmpty),

                infoViolation("_environmentNames", listOf<String>()),
                infoViolation("_colorSchemes", listOf<MapColorScheme>())
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun missing() {
        val ex = validateFolder("info/2_0/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation("_version"),
                infoViolation("_songName"),
                infoViolation("_songSubName"),
                infoViolation("_songAuthorName"),
                infoViolation("_levelAuthorName"),
                infoViolation("_beatsPerMinute"),
                infoViolation("_songTimeOffset"),
                infoViolation("_shuffle"),
                infoViolation("_shufflePeriod"),
                infoViolation("_previewStartTime"),
                infoViolation("_previewDuration"),
                infoViolation("_songFilename"),
                infoViolation("_coverImageFilename"),
                infoViolation("_environmentName"),
                infoViolation("_allDirectionsEnvironmentName"),
                infoViolation("_difficultyBeatmapSets")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun badtypes() {
        val ex = validateFolder("info/2_0/badtypes")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolationWrong("_version"),
                infoViolationWrong("_songName"),
                infoViolationWrong("_songSubName"),
                infoViolationWrong("_songAuthorName"),
                infoViolationWrong("_levelAuthorName"),
                infoViolationWrong("_beatsPerMinute"),
                infoViolationWrong("_songTimeOffset"),
                infoViolationWrong("_shuffle"),
                infoViolationWrong("_shufflePeriod"),
                infoViolationWrong("_previewStartTime"),
                infoViolationWrong("_previewDuration"),
                infoViolationWrong("_songFilename"),
                infoViolationWrong("_coverImageFilename"),
                infoViolationWrong("_environmentName"),
                infoViolationWrong("_allDirectionsEnvironmentName"),
                infoViolationWrong("_difficultyBeatmapSets")
            ),
            ex.constraintViolations
        )
    }
}
