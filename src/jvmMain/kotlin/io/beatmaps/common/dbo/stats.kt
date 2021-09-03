package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Plays : IntIdTable("plays", "Id") {
    val mapId = reference("mapId", Beatmap)
    val userId = long("userId")
    val createdAt = timestamp("createdAt")
}

object Votes : IntIdTable("vote", "Id") {
    val mapId = reference("mapId", Beatmap)
    val userId = long("userId")
    val vote = bool("vote")
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val steam = bool("steam")
}

data class VotesDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<VotesDao>(Votes)
    val userId by Votes.userId
    val vote by Votes.vote
    val steam by Votes.steam
}

object Downloads : IntIdTable("downloads", "downloadId") {
    val hash = char("hash", 40)
    val remote = varchar("remote", 15)
    val processed = bool("processed")
}

data class DownloadsDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<DownloadsDao>(Downloads)
    val hash: String by Downloads.hash
    val remote: String by Downloads.remote
    val processed: Boolean by Downloads.processed
}
