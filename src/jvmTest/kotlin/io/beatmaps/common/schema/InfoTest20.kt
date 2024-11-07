package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.InFiles
import io.beatmaps.common.beatsaber.MetadataLength
import io.beatmaps.common.beatsaber.MisplacedCustomData
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.bpmViolation
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.Equals
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.LessOrEqual
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
                infoViolation<NotNull>("_version"),
                infoViolation<NotNull>("_songName"),
                infoViolation<NotNull>("_songSubName"),
                infoViolation<NotNull>("_songAuthorName"),
                infoViolation<NotNull>("_levelAuthorName"),
                infoViolation<NotNull>("_beatsPerMinute"),
                infoViolation<NotNull>("_shuffle"),
                infoViolation<NotNull>("_shufflePeriod"),
                infoViolation<NotNull>("_songFilename"),
                infoViolation<NotNull>("_coverImageFilename"),
                infoViolation<NotNull>("_customData._contributors[0]._role"),
                infoViolation<NotNull>("_customData._contributors[0]._name"),
                infoViolation<NotNull>("_customData._contributors[0]._iconPath"),
                infoViolation<NotNull>("_customData._contributors[1]"),
                infoViolation<NotNull>("_customData._editors._lastEditedBy"),
                infoViolation<NotNull>("_environmentName"),
                infoViolation<NotNull>("_allDirectionsEnvironmentName"),
                infoViolation<In<String>>("_difficultyBeatmapSets[0]._beatmapCharacteristicName"),
                infoViolation<In<String>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficulty"),
                infoViolation<In<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyRank"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapFilename"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._difficultyLabel"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._editorOffset"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._editorOldOffset"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._warnings[0]"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._information[0]"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._suggestions[0]"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._requirements[0]"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[1]"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._customData._characteristicLabel"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._customData._characteristicIconImageFilename"),
                infoViolation<NotNull>("_difficultyBeatmapSets[1]._difficultyBeatmaps"),
                infoViolation<NotNull>("_difficultyBeatmapSets[2]")
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
                infoViolation<NotNull>("_customData._contributors"),
                infoViolation<NotNull>("_customData._editors.MMA2"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._warnings"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._information"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._suggestions"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._requirements"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._customData")
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
                infoViolation<NotNull>("_customData._editors"),
                infoViolation<NotNull>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData")
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
                infoViolation<NotNull>("_customData"),
                infoViolation<NotNull>("_difficultyBeatmapSets")
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
                infoViolation<NotBlank>("_songName"),
                infoViolation<MetadataLength>("_songName"),
                infoViolation<Between<Float>>("_beatsPerMinute"),
                infoViolation<Equals<Float>>("_songTimeOffset"),
                infoViolation<GreaterOrEqual<Float>>("_previewStartTime"),
                infoViolation<GreaterOrEqual<Float>>("_previewDuration"),
                infoViolation<InFiles>("_songFilename"),
                infoViolation<InFiles>("_coverImageFilename"),

                infoViolation<MisplacedCustomData>("_customData._warnings"),
                infoViolation<MisplacedCustomData>("_customData._information"),
                infoViolation<MisplacedCustomData>("_customData._suggestions"),
                infoViolation<MisplacedCustomData>("_customData._requirements"),
                infoViolation<MisplacedCustomData>("_customData._difficultyLabel"),
                infoViolation<MisplacedCustomData>("_customData._envColorLeft"),
                infoViolation<MisplacedCustomData>("_customData._envColorRight"),
                infoViolation<MisplacedCustomData>("_customData._colorLeft"),
                infoViolation<MisplacedCustomData>("_customData._colorRight"),

                infoViolation<In<String>>("_allDirectionsEnvironmentName"),

                infoViolation<In<String>>("_difficultyBeatmapSets[0]._beatmapCharacteristicName"),

                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._warnings"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._information"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._suggestions"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._requirements"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyLabel"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._envColorLeft"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._envColorRight"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._colorLeft"),
                infoViolation<MisplacedCustomData>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._colorRight"),

                infoViolation<In<String>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficulty"),
                infoViolation<In<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._difficultyRank"),
                infoViolation<InFiles>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapFilename"),
                infoViolation<Less<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx"),
                infoViolation<NodeNotPresent>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx"),
                infoViolation<Less<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx"),
                infoViolation<NodeNotPresent>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx"),

                infoViolation<NotEmpty>("_difficultyBeatmapSets[1]._difficultyBeatmaps"),

                infoViolation<In<String>>("_difficultyBeatmapSets[2]._beatmapCharacteristicName"),
                infoViolation<NotEmpty>("_difficultyBeatmapSets[2]._difficultyBeatmaps"),

                infoViolation<NodeNotPresent>("_environmentNames"),
                infoViolation<NodeNotPresent>("_colorSchemes")
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
                infoViolation<NodePresent>("_version"),
                infoViolation<NodePresent>("_songName"),
                infoViolation<NodePresent>("_songSubName"),
                infoViolation<NodePresent>("_songAuthorName"),
                infoViolation<NodePresent>("_levelAuthorName"),
                infoViolation<NodePresent>("_beatsPerMinute"),
                infoViolation<NodePresent>("_songTimeOffset"),
                infoViolation<NodePresent>("_shuffle"),
                infoViolation<NodePresent>("_shufflePeriod"),
                infoViolation<NodePresent>("_previewStartTime"),
                infoViolation<NodePresent>("_previewDuration"),
                infoViolation<NodePresent>("_songFilename"),
                infoViolation<NodePresent>("_coverImageFilename"),
                infoViolation<NodePresent>("_environmentName"),
                infoViolation<NodePresent>("_difficultyBeatmapSets")
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
                infoViolation<CorrectType>("_version"),
                infoViolation<CorrectType>("_songName"),
                infoViolation<CorrectType>("_songSubName"),
                infoViolation<CorrectType>("_songAuthorName"),
                infoViolation<CorrectType>("_levelAuthorName"),
                infoViolation<CorrectType>("_beatsPerMinute"),
                infoViolation<CorrectType>("_songTimeOffset"),
                infoViolation<CorrectType>("_shuffle"),
                infoViolation<CorrectType>("_shufflePeriod"),
                infoViolation<CorrectType>("_previewStartTime"),
                infoViolation<CorrectType>("_previewDuration"),
                infoViolation<CorrectType>("_songFilename"),
                infoViolation<CorrectType>("_coverImageFilename"),
                infoViolation<CorrectType>("_environmentName"),
                infoViolation<CorrectType>("_allDirectionsEnvironmentName"),
                infoViolation<CorrectType>("_difficultyBeatmapSets")
            ),
            ex.constraintViolations
        )
    }
}
