package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Follows : IntIdTable("follows", "followId") {
    val userId = reference("userId", User)
    val followerId = reference("followerId", User)

    val since = timestamp("since")
}

data class FollowsDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<FollowsDao>(Follows)
    val user by UserDao referencedOn Follows.userId
    val follower by UserDao referencedOn Follows.followerId

    val since by Follows.since
}
