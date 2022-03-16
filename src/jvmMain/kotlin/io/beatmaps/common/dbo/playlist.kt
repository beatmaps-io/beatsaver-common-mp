package io.beatmaps.common.dbo

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
import org.jetbrains.exposed.sql.javatime.timestamp

object Playlist : IntIdTable("playlist", "playlistId") {
    fun joinMaps(type: JoinType = JoinType.LEFT, state: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        join(PlaylistMap, type, Playlist.id, PlaylistMap.playlistId, state)

    val name = varchar("name", 255)
    val owner = reference("owner", User)

    val description = text("description")
    val public = bool("public")

    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val deletedAt = timestamp("deletedAt").nullable()
    val songsChangedAt = timestamp("songsChangedAt").nullable()

    val curator = optReference("curatedBy", User)
    val curatedAt = timestamp("curatedAt").nullable()
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
    val public by Playlist.public

    val createdAt by Playlist.createdAt
    val updatedAt by Playlist.updatedAt
    val deletedAt by Playlist.deletedAt
    val songsChangedAt by Playlist.songsChangedAt

    val curatedAt by Playlist.curatedAt
    val curator by UserDao optionalReferencedOn Playlist.curator
}

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

    val order by PlaylistMap.order
}
