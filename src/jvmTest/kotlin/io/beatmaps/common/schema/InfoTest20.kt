package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.*
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.infoViolationWrong
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.constraints.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoTest20 {
    @Test
    fun basic() {
        val ex = validateFolder("info/2_0/basic")
        assertNull(ex)
    }

    @Test
    fun error() {
        val ex = validateFolder("info/2_0/error")
        assertNotNull(ex)

        assertEquals(
            setOf(
                infoViolation("_songName", "", NotBlank),
                infoViolation("_songName", "", MetadataLength),
                infoViolation("_beatsPerMinute", 1200f, Between(10f, 1000f)),
                infoViolation("_previewStartTime", -10f, GreaterOrEqual(0f)),
                infoViolation("_previewDuration", -10f, GreaterOrEqual(0f)),
                infoViolation("_songFilename", "missing.ogg", InFiles),
                infoViolation("_coverImageFilename", "missing.png", InFiles),
                infoViolation("_allDirectionsEnvironmentName", "MuliplayerEnvironment", In(setOf("GlassDesertEnvironment"))),
                infoViolation("_songTimeOffset", 1f, Equals(0f)),

                infoViolation("_difficultyBeatmapSets[0]._beatmapCharacteristicName", "Legacy", In(setOf("Standard", "NoArrows", "OneSaber", "360Degree", "90Degree", "Lightshow", "Lawless"))),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficulty", "Mid", In(setOf("Easy", "Normal", "Hard", "Expert", "ExpertPlus"))),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyRank", 2, In(listOf(1, 3, 5, 7, 9))),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapFilename", "missing.dat", InFiles),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx", 1, Less(1)),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx", 1),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx", 1, Less(1)),
                infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx", 1),

                infoViolation("_difficultyBeatmapSets[1]._beatmapCharacteristicName", "Easy", In(setOf("Standard", "NoArrows", "OneSaber", "360Degree", "90Degree", "Lightshow", "Lawless"))),
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

        assertEquals(
            setOf(
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

        assertEquals(
            setOf(
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
