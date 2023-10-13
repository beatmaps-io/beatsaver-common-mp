package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.BSDiff
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
    fun validateFolder(name: String, diffValidator: ((BSDiff) -> Unit)? = null): ConstraintViolationException? {
        val info = javaClass.getResourceAsStream("/$name/Info.dat")!!
        val audio = File(javaClass.getResource("/shared/click.ogg")!!.toURI())
        val files = listOf("Info.dat", "Easy.dat", "click.ogg", "click.png")
        val md = MessageDigest.getInstance("SHA1")

        val str = readFromBytes(info.readAllBytes()).replace("\r\n", "\n")
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
                diffValidator?.invoke(extractedInfo.diffs.values.first().values.first())
            }
        } catch (e: ConstraintViolationException) {
            return e
        }

        // Check encoded version matches original
        assertEquals(str, mapInfo.toJson())

        return null
    }

    inline fun <reified T : Constraint> violation(prop: String) =
        infoViolation<T>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop")

    inline fun <reified T : Constraint> infoViolation(prop: String) =
        Violation(prop, T::class)
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
