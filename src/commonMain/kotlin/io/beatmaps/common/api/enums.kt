package io.beatmaps.common.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface HumanEnum<E> where E : Enum<E>, E : HumanEnum<E> {
    fun human(): String
}

enum class AiDeclarationType(val markAsBot: Boolean = true, val override: Boolean = false) {
    Admin, Uploader, SageScore(override = true), None(false, true)
}

@Serializable
enum class ECharacteristic(private val ename: String, val color: String, val rotation: Boolean) : HumanEnum<ECharacteristic> {
    Standard("Standard", "primary", false),
    OneSaber("OneSaber", "info", false),
    NoArrows("NoArrows", "info", false),

    @SerialName("90Degree")
    Rotation90Degree("90Degree", "warning", true),

    @SerialName("360Degree")
    Rotation360Degree("360Degree", "warning", true),

    Lightshow("Lightshow", "danger", false),
    Lawless("Lawless", "danger", false),
    Legacy("Legacy", "danger", false);

    override fun human() = ename

    companion object {
        private val map = entries.associateBy { it.ename }
        fun fromName(name: String) = fromNameOrNull(name) ?: throw IllegalArgumentException("No characteristic for $name")
        fun fromNameOrNull(name: String) = map[name]
    }
}

@Serializable
enum class EDifficulty(val idx: Int, private val _human: String, val color: String) : HumanEnum<EDifficulty> {
    Easy(1, "Easy", "green"),
    Normal(3, "Normal", "blue"),
    Hard(5, "Hard", "hard"),
    Expert(7, "Expert", "expert"),
    ExpertPlus(9, "Expert+", "purple");

    override fun human() = _human

    companion object {
        private val map = entries.associateBy(EDifficulty::idx)
        fun fromInt(type: Int) = map[type]

        private val nameMap = entries.associateBy { it.name }
        fun fromName(name: String) = nameMap[name] ?: throw IllegalArgumentException("No difficulty for $name")
    }
}

enum class EMapState {
    Uploaded, Testplay, Published, Feedback, Scheduled
}

enum class EAlertType(val color: String, val icon: String, private val readableName: String? = null) {
    Deletion("danger", "fa-exclamation-circle"),
    Review("info", "fa-comment"),
    ReviewReply("info", "fa-reply"),
    ReviewDeletion("danger", "fa-comment-slash", "Review Deletion"),
    MapRelease("info", "fa-map", "Map Release"),
    MapCurated("info", "fa-award", "Followed Curation"),
    Curation("success", "fa-award"),
    Follow("success", "fa-user-plus"),
    Uncuration("danger", "fa-award"),
    Collaboration("warning", "fa-user-friends"),
    Issue("info", "fa-flag");

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

        val publicTypes = entries.filter { it.anonymousAllowed }
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

/**
 * Instructions for checking the official environments list:
 * - Open Official Editor and Load Any Map
 * - Open RUE
 * - Search Components for "DifficultyBeatmapView" -> Select Component
 * - Filter fields for "_environmentsListModel"
 * - Use "_normalEnvironmentSerializedNames", optionally check "_normalEnvironmentNames"
 */
@Serializable
enum class EBeatsaberEnvironment(private val short: String, val rotation: Boolean, val v3: Boolean, val filterable: Boolean = true) : HumanEnum<EBeatsaberEnvironment> {
    DefaultEnvironment("Default", false, false),
    TriangleEnvironment("Triangle", false, false),
    NiceEnvironment("Nice", false, false),
    BigMirrorEnvironment("Big Mirror", false, false),
    KDAEnvironment("KDA", false, false),
    MonstercatEnvironment("Monstercat", false, false),
    CrabRaveEnvironment("Crab Rave", false, false),
    DragonsEnvironment("Dragons", false, false),
    OriginsEnvironment("Origins", false, false),
    PanicEnvironment("Panic", false, false),
    RocketEnvironment("Rocket", false, false),
    GreenDayEnvironment("Green Day", false, false),
    GreenDayGrenadeEnvironment("Green Day Grenade", false, false),
    TimbalandEnvironment("Timbaland", false, false),
    FitBeatEnvironment("Fitbeat", false, false),
    LinkinParkEnvironment("Linkin Park", false, false),
    BTSEnvironment("BTS", false, false),
    KaleidoscopeEnvironment("Kaleidoscope", false, false),
    InterscopeEnvironment("Interscope", false, false),
    SkrillexEnvironment("Skrillex", false, false),
    BillieEnvironment("Billie", false, false),
    HalloweenEnvironment("Halloween", false, false),
    GagaEnvironment("Gaga", false, false),
    Halloween2Environment("Spoooky", false, false),

    GlassDesertEnvironment("Glass Desert", true, false),
    MultiplayerEnvironment("Multiplayer", false, false, filterable = false),

    WeaveEnvironment("Weave", false, true),
    PyroEnvironment("Pyro", false, true),
    EDMEnvironment("EDM", false, true),
    TheSecondEnvironment("The Second", false, true),
    LizzoEnvironment("Lizzo", false, true),
    TheWeekndEnvironment("The Weeknd", false, true),
    RockMixtapeEnvironment("Rock Mixtape", false, true),
    Dragons2Environment("Dragons 2", false, true),
    Panic2Environment("Panic 2", false, true),
    QueenEnvironment("Queen", false, true),
    LinkinPark2Environment("Linkin Park 2", false, true),
    TheRollingStonesEnvironment("The Rolling Stones", false, true),
    LatticeEnvironment("Lattice", false, true),
    DaftPunkEnvironment("Daft Punk", false, true),
    HipHopEnvironment("HipHop", false, true),
    ColliderEnvironment("Collider", false, true),
    BritneyEnvironment("Britney", false, true),
    Monstercat2Environment("Monstercat 2", false, true),
    MetallicaEnvironment("Metallica", false, true),
    GridEnvironment("Cube", false, true),
    ColdplayEnvironment("Coldplay", false, true);

    fun color() = when {
        v3 -> "purple"
        rotation -> "green"
        else -> "blue"
    }

    fun category() = if (v3) "New" else "Legacy"

    override fun human() = short

    companion object {
        val names = entries.map { it.name }.toSet()

        private val map = entries.associateBy(EBeatsaberEnvironment::name)
        fun fromString(type: String?) = map[type]
    }
}
