@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.properties.ReadOnlyProperty

interface BSCustomData {
    val _customData: Any?

    fun getCustomData() = when (_customData) {
        is Map<*, *> -> _customData
        else -> mapOf<String, String>()
    } as Map<*, *>
}

fun <T : BSCustomData> List<T>.withoutFake() = this.filter { obj -> (obj.getCustomData()["_fake"] as? JsonPrimitive)?.booleanOrNull != true }

fun <T> orNegativeInfinity(block: (T) -> OptionalProperty<Float?>): ReadOnlyProperty<T, Float> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).orNull() ?: Float.NEGATIVE_INFINITY }
fun <T> orMinValue(block: (T) -> OptionalProperty<Int?>): ReadOnlyProperty<T, Int> =
    ReadOnlyProperty { thisRef, _ -> block(thisRef).orNull() ?: Int.MIN_VALUE }

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
