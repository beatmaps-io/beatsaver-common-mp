package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object ReviewSilence : IntIdTable("review_silence", "silenceId") {
    val userId = reference("userId", User)
    val moderatorId = reference("moderatorId", User)
    val createdAt = timestamp("createdAt")
    val silencedUntil = timestamp("silencedUntil").nullable()
    val durationMinutes = integer("durationMinutes").nullable()
    val reason = text("reason").nullable()
    val revokedAt = timestamp("revokedAt").nullable()
}
