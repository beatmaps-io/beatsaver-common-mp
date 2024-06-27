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

enum class AiDeclarationType(val markAsBot: Boolean = true, val override: Boolean = false) {
    Admin, Uploader, SageScore(override = true), None(false, true)
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

inline fun <reified T : Enum<T>> searchEnumOrNull(search: String) =
    search.replace(" ", "").let { sanitized ->
        enumValues<T>().firstOrNull { each ->
            each.name.removePrefix("_").compareTo(sanitized, ignoreCase = true) == 0
        }
    }

inline fun <reified T : Enum<T>> searchEnum(search: String) =
    searchEnumOrNull<T>(search) ?: throw IllegalArgumentException("No enum constant for search $search")

@Serializable(with = ECharacteristicSerializer::class)
@Suppress("ktlint:standard:enum-entry-name-case")
enum class ECharacteristic(val color: String) : HumanEnum<ECharacteristic> {
    Standard("primary"), OneSaber("info"), NoArrows("info"), _90Degree("warning"), _360Degree("warning"), Lightshow("danger"),
    Lawless("danger"), Legacy("danger");

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
        private val map = entries.associateBy(EDifficulty::idx)
        fun fromInt(type: Int) = map[type]
    }
}

enum class EMapState {
    Uploaded, Testplay, Published, Feedback, Scheduled
}

enum class EAlertType(val color: String, val icon: String, private val readableName: String? = null) {
    Deletion("danger", "fa-exclamation-circle"),
    Review("info", "fa-comment"),
    ReviewDeletion("danger", "fa-comment-slash", "Review Deletion"),
    MapRelease("info", "fa-map", "Map Release"),
    MapCurated("info", "fa-award", "Followed Curation"),
    Curation("success", "fa-award"),
    Follow("success", "fa-user-plus"),
    Uncuration("danger", "fa-award"),
    Collaboration("warning", "fa-user-friends");

    fun readable(): String = readableName ?: name

    companion object {
        private val map = entries.associateBy { it.name.lowercase() }
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

enum class EPlaylistType(val anonymousAllowed: Boolean, val orderable: Boolean) {
    Private(false, true),
    Public(true, true),
    System(false, true),
    Search(true, false);

    companion object {
        private val map = entries.associateBy(EPlaylistType::name)
        fun fromString(type: String?) = map[type]
    }
}

enum class RankedFilter(val blRanked: Boolean = false, val ssRanked: Boolean = false) {
    All,
    Ranked(true, true),
    BeatLeader(blRanked = true),
    ScoreSaber(ssRanked = true);

    companion object {
        private val map = RankedFilter.entries.associateBy(RankedFilter::name)
        fun fromString(type: String?) = map[type]
    }
}
