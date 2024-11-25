package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.InFiles
import io.beatmaps.common.beatsaber.MetadataLength
import io.beatmaps.common.beatsaber.MultipleVersionsConstraint
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.bpmViolation
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.GreaterOrEqual
import org.valiktor.constraints.In
import org.valiktor.constraints.Less
import org.valiktor.constraints.LessOrEqual
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotBlank
import org.valiktor.constraints.NotEmpty
import org.valiktor.constraints.NotNull
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoTest40 {
    @Test
    fun basic() {
        val ex = validateFolder("info/4_0/basic")
        assertNull(ex)
    }

    @Test
    fun with2_0() {
        val ex = validateFolder("info/4_0/with2_0")
        assertNull(ex)
    }

    @Test
    fun as2_0() {
        val ex = validateFolder("info/4_0/as2_0")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation<MultipleVersionsConstraint>("version/_version")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun error() {
        val ex = validateFolder("info/4_0/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                bpmViolation<NotNull>("bpmData[0].si(startSampleIndex)"),
                bpmViolation<NotNull>("bpmData[0].ei(endSampleIndex)"),
                bpmViolation<NotNull>("bpmData[0].sb(startBeat)"),
                bpmViolation<NotNull>("bpmData[0].eb(endBeat)"),
                bpmViolation<CorrectType>("bpmData[1].si(startSampleIndex)"),
                bpmViolation<CorrectType>("bpmData[1].ei(endSampleIndex)"),
                bpmViolation<CorrectType>("bpmData[1].sb(startBeat)"),
                bpmViolation<CorrectType>("bpmData[1].eb(endBeat)"),
                bpmViolation<NodePresent>("bpmData[2].si(startSampleIndex)"),
                bpmViolation<NodePresent>("bpmData[2].ei(endSampleIndex)"),
                bpmViolation<NodePresent>("bpmData[2].sb(startBeat)"),
                bpmViolation<NodePresent>("bpmData[2].eb(endBeat)"),
                bpmViolation<GreaterOrEqual<Int>>("bpmData[3].ei(endSampleIndex)"),
                bpmViolation<GreaterOrEqual<Int>>("bpmData[3].eb(endBeat)"),
                bpmViolation<GreaterOrEqual<Int>>("bpmData[4].si(startSampleIndex)"),
                bpmViolation<LessOrEqual<Int>>("bpmData[4].ei(endSampleIndex)"),
                bpmViolation<GreaterOrEqual<Int>>("bpmData[4].sb(startBeat)"),

                infoViolation<NotBlank>("song.title"),
                infoViolation<MetadataLength>("song"),
                infoViolation<InFiles>("audio.songFilename"),
                infoViolation<Between<Float>>("audio.bpm"),
                infoViolation<GreaterOrEqual<Float>>("audio.previewStartTime"),
                infoViolation<GreaterOrEqual<Float>>("audio.previewDuration"),
                infoViolation<InFiles>("songPreviewFilename"),
                infoViolation<InFiles>("coverImageFilename"),
                infoViolation<In<String>>("environmentNames[0]"),

                infoViolation<Matches>("colorSchemes[0].saberAColor"),
                infoViolation<Matches>("colorSchemes[0].saberBColor"),

                infoViolation<In<String>>("difficultyBeatmaps[0].difficulty"),
                infoViolation<Less<Int>>("difficultyBeatmaps[0].environmentNameIdx"),
                infoViolation<Less<Int>>("difficultyBeatmaps[0].beatmapColorSchemeIdx"),

                infoViolation<InFiles>("difficultyBeatmaps[0].beatmapDataFilename"),
                infoViolation<InFiles>("difficultyBeatmaps[0].lightshowDataFilename")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun missing() {
        val ex = validateFolder("info/4_0/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation<NodePresent>("song.title"),
                infoViolation<NodePresent>("song.subTitle"),
                infoViolation<NodePresent>("song.author"),
                infoViolation<NodePresent>("audio.songFilename"),
                infoViolation<NodePresent>("audio.songDuration"),
                infoViolation<NodePresent>("audio.audioDataFilename"),
                infoViolation<NodePresent>("audio.bpm"),
                infoViolation<NodePresent>("audio.lufs"),
                infoViolation<NodePresent>("audio.previewStartTime"),
                infoViolation<NodePresent>("audio.previewDuration"),
                infoViolation<NodePresent>("songPreviewFilename"),
                infoViolation<NodePresent>("coverImageFilename"),
                infoViolation<NotEmpty>("environmentNames"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].characteristic"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].difficulty"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].beatmapAuthors.mappers"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].beatmapAuthors.lighters"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].environmentNameIdx"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].beatmapColorSchemeIdx"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].noteJumpMovementSpeed"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].noteJumpStartBeatOffset"),
                infoViolation<NodePresent>("difficultyBeatmaps[0].beatmapDataFilename")
            ),
            ex.constraintViolations
        )
    }
}
