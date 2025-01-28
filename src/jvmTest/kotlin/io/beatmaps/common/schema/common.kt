package io.beatmaps.common.schema

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.info.BaseMapInfo
import io.beatmaps.common.beatsaber.info.check
import io.beatmaps.common.beatsaber.info.toJson
import io.beatmaps.common.beatsaber.map.BSDiff
import io.beatmaps.common.beatsaber.map.BSLights
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import io.beatmaps.common.zip.readFromBytes
import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.ConstraintViolationException
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.test.assertEquals

object SchemaCommon {
    fun validateFolder(name: String, diffValidators: List<DiffValidator> = listOf()): ConstraintViolationException? {
        val info = javaClass.getResourceAsStream("/$name/Info.dat")!!
        val audio = File(javaClass.getResource("/shared/click.ogg")!!.toURI())
        val files = listOf("Info.dat", "Easy.dat", "Lights.dat", "BPMInfo.dat", "click.ogg", "click.png")

        val str = readFromBytes(info.readAllBytes()).replace("\r\n", "\n")
        val jsonElement = jsonIgnoreUnknown.parseToJsonElement(str)

        try {
            val mapInfo = BaseMapInfo.parse(jsonElement).check()

            ByteArrayOutputStream().use { toHash ->
                val extractedInfo = ExtractedInfo(files, toHash, mapInfo, 0)
                mapInfo.validate(files.map { it.lowercase() }.toSet(), extractedInfo, audio, audio) {
                    if (files.contains(it)) {
                        object : IZipPath {
                            override fun inputStream() = (if (setOf("ogg", "png").contains(it.substringAfterLast("."))) "shared" else name).let { fn ->
                                javaClass.getResourceAsStream("/$fn/$it")
                            }

                            override val fileName = it
                            override val compressedSize = 0L
                        }
                    } else {
                        null
                    }
                }
                diffValidators.forEach {
                    val char = extractedInfo.diffs.filter { d -> d.key == it.characteristic }
                    val charLights = extractedInfo.lights.filter { d -> d.key == it.characteristic }
                    assert(char.size == 1 && charLights.size == 1) { "Missing or multiple characteristics for validator" }

                    val diff = char.entries.first().value.filter { d -> d.key.enumValue() == it.difficulty }
                    val diffLights = charLights.entries.first().value.filter { d -> d.key.enumValue() == it.difficulty }
                    assert(diff.size == 1 && diffLights.size == 1) { "Missing or multiple difficulty for validator" }

                    it.block.invoke(diff.values.first(), diffLights.values.first(), extractedInfo.songLengthInfo!!)
                }
            }

            // Check encoded version matches original
            assertEquals(str, mapInfo.toJson())
        } catch (e: ConstraintViolationException) {
            return e
        }

        return null
    }

    inline fun <reified T : Constraint> violation(prop: String) =
        infoViolation<T>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop")

    inline fun <reified T : Constraint> violation4(prop: String) =
        infoViolation<T>("difficultyBeatmaps[0].`Easy.dat`.$prop")

    inline fun <reified T : Constraint> violation4L(prop: String) =
        infoViolation<T>("difficultyBeatmaps[0].`Lights.dat`.$prop")

    inline fun <reified T : Constraint> bpmViolation(prop: String) =
        infoViolation<T>("`BPMInfo.dat`.$prop")

    inline fun <reified T : Constraint> infoViolation(prop: String) =
        Violation(prop, T::class)
}

data class DiffValidator(val characteristic: ECharacteristic, val difficulty: EDifficulty, val block: (BSDiff, BSLights, SongLengthInfo) -> Unit)

data class Violation<T : Constraint>(override val property: String, val constraintType: KClass<T>) : ConstraintViolation {
    // Will generally error but isn't intended to be used
    override val constraint: T by lazy { constraintType.createInstance() }
    override val value: Any? = null

    override fun equals(other: Any?): Boolean {
        if (other is ConstraintViolation) {
            return other.property == property && constraintType.isInstance(other.constraint)
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = property.hashCode()
        result = 31 * result + constraintType.hashCode()
        return result
    }
}
