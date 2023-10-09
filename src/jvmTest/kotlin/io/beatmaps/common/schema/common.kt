package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
import kotlinx.serialization.json.decodeFromJsonElement
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import java.io.File
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.test.assertEquals

object SchemaCommon {
    fun validateFolder(name: String): ConstraintViolationException? {
        val info = javaClass.getResourceAsStream("/$name/Info.dat")!!
        val audio = File(javaClass.getResource("/shared/click.ogg")!!.toURI())
        val files = listOf("Info.dat", "Easy.dat", "click.ogg", "click.png")
        val md = MessageDigest.getInstance("SHA1")

        val str = readFromBytes(info.readAllBytes())
        val jsonElement = jsonIgnoreUnknown.parseToJsonElement(str)
        val mapInfo = jsonIgnoreUnknown.decodeFromJsonElement<MapInfo>(jsonElement)

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

        // Check encoded version matches original
        assertEquals(str, mapInfo.toJson())

        return null
    }

    fun violation(prop: String) =
        infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop")

    fun violationWrong(prop: String) =
        infoViolationWrong("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop")

    fun infoViolationWrong(prop: String) =
        DefaultConstraintViolation(prop, OptionalProperty.WrongType, CorrectType)

    fun violation(prop: String, v: Any?, constraint: Constraint = NodeNotPresent) =
        infoViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop", v, constraint)

    fun <T : Constraint> partialViolation(prop: String, constraint: KClass<T>) =
        infoPartialViolation("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop", constraint)

    fun infoViolation(prop: String, constraint: Constraint = NodePresent) =
        DefaultConstraintViolation(prop, OptionalProperty.NotPresent, constraint)

    fun <T : Constraint> infoPartialViolation(prop: String, constraint: KClass<T>) =
        Violation(prop, constraint)

    fun infoViolation(prop: String, v: Any?, constraint: Constraint = NodeNotPresent) =
        DefaultConstraintViolation(prop, OptionalProperty.Present(v), constraint)
}

data class Violation<T : Constraint>(val prop: String, val constraintType: KClass<T>) {
    override fun equals(other: Any?): Boolean {
        if (other is DefaultConstraintViolation) {
            return other.property == prop && constraintType.isInstance(other.constraint)
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = prop.hashCode()
        result = 31 * result + constraintType.hashCode()
        return result
    }
}
