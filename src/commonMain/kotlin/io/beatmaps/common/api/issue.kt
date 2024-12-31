package io.beatmaps.common.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class EIssueType(private val _human: String) : HumanEnum<EIssueType> {
    MapperApplication("Verified Mapper Application"),
    MapReport("Map Report"),
    UserReport("User Report"),
    PlaylistReport("Playlist Report"),
    ReviewReport("Review Report");

    override fun enumName() = name
    override fun human() = _human

    companion object {
        private val map = entries.associateBy(EIssueType::name)
        fun fromName(name: String?) = map[name]
    }
}

// Base interface / classes
interface IIssueData {
    val typeEnum: EIssueType
}

abstract class MapReportDataBase : IIssueData {
    abstract val mapId: String
    fun id() = mapId.toIntOrNull(16)
    override val typeEnum = EIssueType.MapReport
}

abstract class PlaylistReportDataBase : IIssueData {
    abstract val playlistId: Int
    override val typeEnum = EIssueType.PlaylistReport
}

abstract class UserReportDataBase : IIssueData {
    abstract val userId: Int
    override val typeEnum = EIssueType.UserReport
}

abstract class ReviewReportDataBase : IIssueData {
    abstract val reviewId: Int
    override val typeEnum = EIssueType.ReviewReport
}

// Classes for db serialization. Should contain no extra fields or logic
@Serializable
sealed interface IDbIssueData : IIssueData

@Serializable
@SerialName("MapReport")
data class MapReportData(override val mapId: String) : IDbIssueData, MapReportDataBase()

@Serializable
@SerialName("PlaylistReport")
data class PlaylistReportData(override val playlistId: Int) : IDbIssueData, PlaylistReportDataBase()

@Serializable
@SerialName("UserReport")
data class UserReportData(override val userId: Int) : IDbIssueData, UserReportDataBase()

@Serializable
@SerialName("ReviewReport")
data class ReviewReportData(override val reviewId: Int) : IDbIssueData, ReviewReportDataBase()
