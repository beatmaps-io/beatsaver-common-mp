package io.beatmaps.common.schema

import com.fasterxml.jackson.module.kotlin.readValue
import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.jackson
import io.beatmaps.common.zip.ExtractedInfo
import io.beatmaps.common.zip.IZipPath
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import java.io.File
import java.io.OutputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

object SchemaCommon {
    fun validateFolder(name: String): ConstraintViolationException? {
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

    fun violation(prop: String) =
        DefaultConstraintViolation(
            "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop",
            OptionalProperty.NotPresent,
            NodePresent
        )

    fun violation(prop: String, v: Any?, constraint: Constraint = NodeNotPresent) =
        DefaultConstraintViolation(
            "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.$prop",
            OptionalProperty.Present(v),
            constraint
        )
}
