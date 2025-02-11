package io.beatmaps.common.api

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class EIssueType(private val _human: String, val curatorAllowed: Boolean = false) : HumanEnum<EIssueType> {
    MapperApplication("Verified Mapper Application"),
    MapReport("Map Report"),
    UserReport("User Report"),
    PlaylistReport("Playlist Report"),
    ReviewReport("Review Report", true);

    override fun human() = _human

    companion object {
        private val map = entries.associateBy(EIssueType::name)
        fun fromName(name: String?) = map[name]

        val curatorTypes = entries.filter(EIssueType::curatorAllowed)
    }
}

@Serializable
data class BasicMapInfo(
    val key: String,
    val name: String,
    val description: String,
    val declaredAi: AiDeclarationType,
    val uploaded: Instant?,
    val hash: String,
    val mapper: BasicUserInfo,
    val bpm: Float,
    val duration: Int
)

@Serializable
data class BasicUserInfo(
    val id: Int,
    val name: String,
    val description: String,
    val avatar: String
)

@Serializable
data class BasicPlaylistInfo(
    val id: Int,
    val name: String,
    val description: String,
    val owner: BasicUserInfo
)

@Serializable
data class BasicReviewInfo(
    val id: Int,
    val text: String,
    val sentiment: ReviewSentiment,
    val map: BasicMapInfo,
    val creator: BasicUserInfo,
    val createdAt: Instant
)

// Base interface / classes
interface IIssueData {
    val typeEnum: EIssueType
}

abstract class MapReportDataBase : IIssueData {
    abstract val mapId: String
    abstract val snapshot: BasicMapInfo?
    fun id() = mapId.toIntOrNull(16)
    override val typeEnum = EIssueType.MapReport
}

abstract class PlaylistReportDataBase : IIssueData {
    abstract val playlistId: Int
    abstract val snapshot: BasicPlaylistInfo?
    override val typeEnum = EIssueType.PlaylistReport
}

abstract class UserReportDataBase : IIssueData {
    abstract val userId: Int
    abstract val snapshot: BasicUserInfo?
    override val typeEnum = EIssueType.UserReport
}

abstract class ReviewReportDataBase : IIssueData {
    abstract val reviewId: Int
    abstract val snapshot: BasicReviewInfo?
    override val typeEnum = EIssueType.ReviewReport
}

// Classes for db serialization
@Serializable
sealed interface IDbIssueData : IIssueData

@Serializable
@SerialName("MapReport")
data class DbMapReportData(
    override val mapId: String,
    override val snapshot: BasicMapInfo? = null
) : IDbIssueData, MapReportDataBase()

@Serializable
@SerialName("PlaylistReport")
data class DbPlaylistReportData(
    override val playlistId: Int,
    override val snapshot: BasicPlaylistInfo? = null
) : IDbIssueData, PlaylistReportDataBase()

@Serializable
@SerialName("UserReport")
data class DbUserReportData(
    override val userId: Int,
    override val snapshot: BasicUserInfo? = null
) : IDbIssueData, UserReportDataBase()

@Serializable
@SerialName("ReviewReport")
data class DbReviewReportData(
    override val reviewId: Int,
    override val snapshot: BasicReviewInfo? = null
) : IDbIssueData, ReviewReportDataBase()
