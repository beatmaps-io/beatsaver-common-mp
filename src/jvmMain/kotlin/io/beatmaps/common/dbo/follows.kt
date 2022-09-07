package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Follows: IntIdTable("follows", "followId") {
    val userId = reference("userId", User)
    val followerId = reference("followerId", User)

    val since = timestamp("since")
}
