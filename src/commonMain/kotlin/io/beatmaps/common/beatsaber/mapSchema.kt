@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.or
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.properties.ReadOnlyProperty

interface BSCustomData {
    val _customData: OptionalProperty<Any?>

    fun getCustomData() = _customData.let { cd ->
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

fun <T> OptionalProperty<List<OptionalProperty<T?>>?>.orEmpty() = or(listOf()).mapNotNull { it.orNull() }

sealed interface BSDiff : BSCustomData {
    val version: OptionalProperty<String?>
    fun noteCount(): Int
    fun bombCount(): Int
    fun arcCount(): Int
    fun chainCount(): Int
    fun eventCount(): Int
    fun obstacleCount(): Int
    fun songLength(): Float
    fun maxScore(): Int
    fun mappedNps(sli: SongLengthInfo): Float
}
