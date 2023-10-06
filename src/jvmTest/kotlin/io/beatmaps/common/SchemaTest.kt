package io.beatmaps.common

import com.fasterxml.jackson.module.kotlin.readValue
import io.beatmaps.common.beatsaber.BSFxEventsCollection
import io.beatmaps.common.beatsaber.BSVfxEventBoxGroup
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import org.junit.Test
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.NotNull
import java.io.File
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest {
    private fun validateFolder(name: String): ConstraintViolationException? {
        val info = javaClass.getResourceAsStream("/$name/Info.dat")!!
        val audio = File(javaClass.getResource("/shared/click.ogg")!!.toURI())
        val files = listOf("Info.dat", "Easy.dat", "click.ogg", "click.png")
        val md = MessageDigest.getInstance("SHA1")
        val mapInfo = jackson.readValue<MapInfo>(info)

        try {
            DigestOutputStream(OutputStream.nullOutputStream(), md).use { dos ->
                val extractedInfo = ExtractedInfo(files, dos, mapInfo, 0)
                mapInfo.validate(files.map { it.lowercase() }.toSet(), extractedInfo, audio) {
                    if (files.contains(it)) {
                        object : IZipPath {
                            override fun inputStream() = (if (setOf("ogg", "png").contains(it.substringAfterLast("."))) "shared" else name).let { fn ->
                                javaClass.getResourceAsStream("/$fn/$it")
                            }
                            override val fileName = it
                        }
                    } else {
                        null
                    }
                }
            }
        } catch (e: ConstraintViolationException) {
            return e
        }

        return null
    }

    @Test
    fun basic() {
        val ex = validateFolder("basic")
        assertNull(ex)
    }

    @Test
    fun basic2_2() {
        val ex = validateFolder("basic2_2")

        ex?.constraintViolations?.forEach {
            println(it)
        }

        assertNull(ex)
    }

    @Test
    fun schema2_2() {
        val ex = validateFolder("2_2")
        assertNull(ex)
    }

    @Test
    fun error2_2() {
        val ex = validateFolder("error2_2")
        assertNotNull(ex)

        ex.constraintViolations.forEach {
            println(it)
        }

        assertEquals(16, ex.constraintViolations.size)
        assertEquals(
            setOf(
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._type",
                    OptionalProperty.Present(50),
                    In(setOf(0, 1, 3))
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._cutDirection",
                    OptionalProperty.Present(50),
                    CutDirection
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._time",
                    OptionalProperty.Present(100.0f),
                    Between(0.0f, 64f)
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    In(setOf(0, 1, 3))
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._cutDirection",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._lineIndex",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._lineLayer",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._duration",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._lineIndex",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._width",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._value",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                )
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun schema3_2() {
        val ex = validateFolder("3_2")
        assertNull(ex)
    }

    @Test
    fun schema3_2as3_3() {
        val ex = validateFolder("3_2as3_3")
        assertNotNull(ex)

        assertEquals(2, ex.constraintViolations.size)
        assertEquals(setOf(
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.vfxEventBoxGroups",
                OptionalProperty.NotPresent,
                NodePresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._fxEventsCollection",
                OptionalProperty.NotPresent,
                NodePresent
            )
        ), ex.constraintViolations)
    }

    @Test
    fun schema3_2as3_0() {
        val ex = validateFolder("3_2as3_0")
        assertNotNull(ex)

        assertEquals(10, ex.constraintViolations.size)
        assertEquals(setOf(
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                OptionalProperty.Present(0.0f),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                OptionalProperty.Present(0),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                OptionalProperty.Present(0.0f),
                NodeNotPresent
            ),
            DefaultConstraintViolation(
                "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                OptionalProperty.Present(0),
                NodeNotPresent
            )
        ), ex.constraintViolations)
    }

    @Test
    fun schema3_3() {
        val ex = validateFolder("3_3")
        assertNull(ex)
    }

    @Test
    fun schema3_3as3_2() {
        val ex = validateFolder("3_3as3_2")
        assertNotNull(ex)

        assertEquals(125, ex.constraintViolations.size)
        assertEquals(
            setOf(
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bpmEvents[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[0]._time",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[0].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[0].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[1].color",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[1].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[1].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[2].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[2].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[4].color",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[4].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[4].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[4].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[5].color",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[5].direction",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[5].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[5].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[5].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorNotes[6].angleOffset",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bombNotes[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bombNotes[0].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bombNotes[0].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bombNotes[1].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.bombNotes[1].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.obstacles[0].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.obstacles[0].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.obstacles[1].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].color",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].x",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].y",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].tailX",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].tailY",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].tailCutDirection",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.sliders[0].sliderMidAnchorMode",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.burstSliders[0].tailX",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.burstSliders[0].tailY",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[1].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[1].value",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[3].value",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[6].value",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[9].value",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[9].floatValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[10].value",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[10].floatValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[11].floatValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[12].floatValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.basicBeatmapEvents[13].floatValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorBoostBeatmapEvents[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorBoostBeatmapEvents[0].boost",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorBoostBeatmapEvents[2].boost",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.colorBoostBeatmapEvents[4].boost",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param0",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param1",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.reversed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].beatDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].transitionType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.param1",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.reversed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.chunks",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.randomType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.seed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.limit",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.alsoAffectsType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].beatDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionShouldAffectFirstBaseEvent",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].transitionType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].transitionType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].brightness",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.param1",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.reversed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.chunks",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.randomType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.seed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.limit",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.alsoAffectsType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].beatDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionShouldAffectFirstBaseEvent",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].transitionType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[1].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.param1",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.reversed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.chunks",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.randomType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.seed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.limit",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.alsoAffectsType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].beatDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionShouldAffectFirstBaseEvent",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].transitionType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].strobeFrequency",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.param1",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.reversed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].beatDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].rotationDistributionParam",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].flipRotation",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].beat",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].usePreviousEventRotationValue",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].loopsCount",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].rotationDirection",
                    OptionalProperty.NotPresent,
                    NodePresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.vfxEventBoxGroups",
                    OptionalProperty.Present(listOf<BSVfxEventBoxGroup>()),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._fxEventsCollection",
                    OptionalProperty.Present(BSFxEventsCollection(OptionalProperty.Present(listOf()), OptionalProperty.Present(listOf()))),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.useNormalEventsAsCompatibleEvents",
                    OptionalProperty.NotPresent,
                    NodePresent
                )
            ),
            ex.constraintViolations
        )
    }
}