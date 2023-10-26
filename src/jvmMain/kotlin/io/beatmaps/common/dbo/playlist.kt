package io.beatmaps.common.dbo

import io.beatmaps.common.IPlaylistConfig
import io.beatmaps.common.api.EPlaylistType
import io.beatmaps.common.db.json
import io.beatmaps.common.db.postgresEnumeration
import io.beatmaps.common.jsonLenient
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.countDistinct
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum

object Playlist : IntIdTable("playlist", "playlistId") {
    val beatmapSubQuery by lazy {
        Beatmap
            .joinVersions(false)
            .slice(Beatmap.columns)
            .selectAll()
            .alias("maps")
    }

    fun joinMaps(type: JoinType = JoinType.LEFT, state: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        join(PlaylistMap, type, Playlist.id, PlaylistMap.playlistId, state)
            .join(beatmapSubQuery, type, beatmapSubQuery[Beatmap.id], PlaylistMap.mapId) { beatmapSubQuery[Beatmap.deletedAt].isNull() }

    val name = varchar("name", 255)
    val owner = reference("owner", User)

    val description = text("description")

    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val deletedAt = timestamp("deletedAt").nullable()
    val songsChangedAt = timestamp("songsChangedAt").nullable()

    val curator = optReference("curatedBy", User)
    val curatedAt = timestamp("curatedAt").nullable()

    val totalMaps = integer("totalMaps")
    val minNps = decimal("minNps", 8, 3)
    val maxNps = decimal("maxNps", 8, 3)

    val type = postgresEnumeration<EPlaylistType>("type", "playlistType")
    val config = json<IPlaylistConfig>("config", json = jsonLenient).nullable()

    object Stats {
        val mapperCount = beatmapSubQuery[Beatmap.uploader].countDistinct()
        val totalDuration = beatmapSubQuery[Beatmap.duration].sum()
        val totalUpvotes = beatmapSubQuery[Beatmap.upVotesInt].sum()
        val totalDownvotes = beatmapSubQuery[Beatmap.downVotesInt].sum()
        val averageScore = beatmapSubQuery[Beatmap.score].avg(4)

        val all = listOf(mapperCount, totalDuration, totalUpvotes, totalDownvotes, averageScore)
    }
}

fun ColumnSet.joinOwner() = join(User, JoinType.INNER, onColumn = Playlist.owner, otherColumn = User.id)
fun ColumnSet.joinPlaylistCurator() = join(curatorAlias, JoinType.LEFT, onColumn = Playlist.curator, otherColumn = curatorAlias[User.id])

fun Iterable<ResultRow>.handleOwner() = this.map { row ->
    if (row.hasValue(User.id)) {
        UserDao.wrapRow(row)
    }

    row
}

fun Iterable<ResultRow>.handleCurator() = this.map { row ->
    if (row.hasValue(curatorAlias[User.id]) && row[Playlist.curator] != null) {
        UserDao.wrapRow(row, curatorAlias)
    }

    row
}

data class PlaylistDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<PlaylistDao>(Playlist)
    val name by Playlist.name
    val ownerId: EntityID<Int> by Playlist.owner
    val owner by UserDao referencedOn Playlist.owner

    val description by Playlist.description

    val createdAt by Playlist.createdAt
    val updatedAt by Playlist.updatedAt
    val deletedAt by Playlist.deletedAt
    val songsChangedAt by Playlist.songsChangedAt

    val curatedAt by Playlist.curatedAt
    val curator by UserDao optionalReferencedOn Playlist.curator

    val totalMaps by Playlist.totalMaps
    val minNps by Playlist.minNps
    val maxNps by Playlist.maxNps

    val type by Playlist.type
    val config by Playlist.config
}

val bookmark by lazy { PlaylistMap.alias("bookmark") }
object PlaylistMap : IntIdTable("playlist_map", "id") {
    val playlistId = reference("playlistId", Playlist)
    val mapId = reference("mapId", Beatmap)

    val order = float("order")

    val link = Index(listOf(playlistId, mapId), true, "link")
}

data class PlaylistMapDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<PlaylistMapDao>(PlaylistMap)
    val playlist by PlaylistDao referencedOn PlaylistMap.playlistId
    val map by BeatmapDao referencedOn PlaylistMap.mapId

    val playlistId by PlaylistMap.playlistId

    val order by PlaylistMap.order
}
