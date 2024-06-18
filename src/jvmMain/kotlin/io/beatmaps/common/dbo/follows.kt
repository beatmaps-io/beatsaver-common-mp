package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.javatime.timestamp

object Follows : IntIdTable("follows", "followId") {
    val userId = reference("userId", User)
    val followerId = reference("followerId", User)

    val upload = bool("upload")
    val curation = bool("curation")
    val collab = bool("collab")
    val following = bool("following")

    val since = timestamp("since")

    val link = Index(listOf(userId, followerId), true, "follow_link")
}

data class FollowsDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<FollowsDao>(Follows)
    val user by UserDao referencedOn Follows.userId
    val follower by UserDao referencedOn Follows.followerId

    val upload by Follows.upload
    val curation by Follows.curation
    val collab by Follows.collab
    val following by Follows.following

    val since by Follows.since
}
