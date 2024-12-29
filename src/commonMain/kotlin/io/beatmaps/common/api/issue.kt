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

interface IIssueData {
    val typeEnum: EIssueType
}

@Serializable
sealed interface IDbIssueData : IIssueData

interface IMapReportData {
    val mapId: String
}

@Serializable
@SerialName("MapReport")
data class MapReportData(override val mapId: String) : IDbIssueData, IMapReportData {
    fun id() = mapId.toIntOrNull(16)
    override val typeEnum = EIssueType.MapReport
}

interface IUserReportData {
    val userId: Int
}

@Serializable
@SerialName("UserReport")
data class UserReportData(override val userId: Int) : IDbIssueData, IUserReportData {
    override val typeEnum = EIssueType.UserReport
}
