@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.map

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.Version
import io.beatmaps.common.beatsaber.custom.BSCustomData
import io.beatmaps.common.beatsaber.custom.BSMapCustomData
import io.beatmaps.common.beatsaber.custom.BSObjectCustomData
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.or
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.properties.ReadOnlyProperty

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class ValidationName(val value: String)

fun <U : BSObjectCustomData, T : BSCustomData<U>> List<T>.withoutFake() = this.filter { obj -> !obj.customData.orNull()?.fake.or(false) }

fun <T> orNegativeInfinity(block: (T) -> OptionalProperty<Float?>): ReadOnlyProperty<T, Float> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).or(Float.NEGATIVE_INFINITY) }
fun <T> orMinValue(block: (T) -> OptionalProperty<Int?>): ReadOnlyProperty<T, Int> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).or(Int.MIN_VALUE) }

fun <T> OptionalProperty<List<OptionalProperty<T?>>?>?.orEmpty() = or(listOf()).mapNotNull { it.orNull() }
fun <T> OptionalProperty<T?>.mapChanged(changes: Map<T, T>) = changes[orNull()]?.let { OptionalProperty.Present(it) } ?: this

interface BSVersioned {
    val version: OptionalProperty<String?>
}

sealed interface ParseResult<T> {
    data class Success<T>(val data: T) : ParseResult<T>
    class MultipleVersions<T> : ParseResult<T>
}

fun <T> JsonObject.parseBS(newBlock: (Version) -> T, oldBlock: () -> T): ParseResult<T> {
    val newVersion = containsKey("version")
    val oldVersion = containsKey("_version")

    return when {
        newVersion && oldVersion -> ParseResult.MultipleVersions()
        newVersion -> {
            val version = Version(this["version"]?.jsonPrimitive?.contentOrNull)
            ParseResult.Success(newBlock(version))
        }
        else -> ParseResult.Success(oldBlock())
    }
}

sealed interface BSDiff : BSCustomData<BSMapCustomData>, BSVersioned {
    fun noteCount(): Int
    fun bombCount(): Int
    fun arcCount(): Int
    fun chainCount(): Int
    fun obstacleCount(): Int
    fun songLength(): Float
    fun maxScore(): Int
    fun mappedNps(sli: SongLengthInfo): Float

    companion object {
        fun parse(element: JsonElement) =
            element.jsonObject.parseBS({ version ->
                when (version.major) {
                    4 -> jsonIgnoreUnknown.decodeFromJsonElement<BSDifficultyV4>(element)
                    else -> jsonIgnoreUnknown.decodeFromJsonElement<BSDifficultyV3>(element)
                }
            }) {
                jsonIgnoreUnknown.decodeFromJsonElement<BSDifficulty>(element)
            }
    }
}
