@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.jsonIgnoreUnknown
import io.beatmaps.common.or
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.properties.ReadOnlyProperty

interface BSCustomData {
    val customData: OptionalProperty<Any?>

    fun getCustomData() = customData.let { cd ->
        when {
            cd is OptionalProperty.Present && cd.value is JsonObject -> cd.value
            else -> mapOf<String, String>()
        }
    }
}

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class ValidationName(val value: String)

fun <T : BSCustomData> List<T>.withoutFake() = this.filter { obj -> (obj.getCustomData()["_fake"] as? JsonPrimitive)?.booleanOrNull != true }

fun <T> orNegativeInfinity(block: (T) -> OptionalProperty<Float?>): ReadOnlyProperty<T, Float> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).or(Float.NEGATIVE_INFINITY) }
fun <T> orMinValue(block: (T) -> OptionalProperty<Int?>): ReadOnlyProperty<T, Int> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).or(Int.MIN_VALUE) }

fun <T> OptionalProperty<List<OptionalProperty<T?>>?>?.orEmpty() = or(listOf()).mapNotNull { it.orNull() }

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

sealed interface BSDiff : BSCustomData, BSVersioned {
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
