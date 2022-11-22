package io.beatmaps.common.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface HumanEnum<E> where E : Enum<E>, E : HumanEnum<E> {
    fun human(): String
    fun enumName(): String
}

object ECharacteristicSerializer : KHumanEnumSerializer<ECharacteristic>(enumValues())
object EDifficultySerializer : KHumanEnumSerializer<EDifficulty>(enumValues())
open class KHumanEnumSerializer<E>(private val members: Array<E>) : KSerializer<E> where E : Enum<E>, E : HumanEnum<E> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ECharacteristic", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: E) = encoder.encodeString(value.enumName())
    override fun deserialize(decoder: Decoder): E {
        val search = decoder.decodeString()
        for (each in members) {
            if (each.name.removePrefix("_").compareTo(search.replace(" ", ""), ignoreCase = true) == 0) {
                return each
            }
        }
        throw IllegalArgumentException("No enum constant for search $search")
    }
}

inline fun <reified T : Enum<T>> searchEnum(search: String): T {
    for (each in enumValues<T>()) {
        if (each.name.removePrefix("_").compareTo(search.replace(" ", ""), ignoreCase = true) == 0) {
            return each
        }
    }
    throw IllegalArgumentException("No enum constant for search $search")
}

@Serializable(with = ECharacteristicSerializer::class)
enum class ECharacteristic(val color: String) : HumanEnum<ECharacteristic> {
    Standard("primary"), OneSaber("info"), NoArrows("info"), _90Degree("warning"), _360Degree("warning"), Lightshow("danger"), Lawless("danger");

    override fun human() = toString().removePrefix("_")
    override fun enumName() = human()

    companion object
}

@Serializable(with = EDifficultySerializer::class)
enum class EDifficulty(val idx: Int, private val _human: String, val color: String) : HumanEnum<EDifficulty> {
    Easy(1, "Easy", "green"), Normal(3, "Normal", "blue"), Hard(5, "Hard", "hard"),
    Expert(7, "Expert", "expert"), ExpertPlus(9, "Expert+", "purple");

    override fun human() = _human
    override fun enumName() = name

    companion object {
        private val map = values().associateBy(EDifficulty::idx)
        fun fromInt(type: Int) = map[type]
    }
}

enum class EMapState {
    Uploaded, Testplay, Published, Feedback, Scheduled
}

enum class EAlertType(val color: String, val icon: String, private val readableName: String? = null) {
    Deletion("danger", "fa-exclamation-circle"),
    Review("info", "fa-comment-alt"),
    MapRelease("info", "fa-map", "Map Release"),
    Curation("success", "fa-award"),
    Uncuration("danger", "fa-award");

    fun readable(): String = readableName ?: name

    companion object {
        private val map = EAlertType.values().associateBy { it.name.lowercase() }
        fun fromLower(lower: String) = EAlertType.map[lower]
        fun fromList(list: String?) = list?.lowercase()?.split(",")?.mapNotNull { a -> EAlertType.fromLower(a) }
    }
}

enum class MapAttr(val color: String) {
    Curated("success"),
    Qualified("info"),
    Ranked("warning"),
    Verified("bs-purple")
}

enum class EPlaylistType() {
    Private,
    Public,
    System
}
