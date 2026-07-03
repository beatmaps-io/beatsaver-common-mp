package io.beatmaps.common.dbo

import io.beatmaps.common.api.SuspensionType
import io.beatmaps.common.db.postgresEnumeration
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Suspensions : IntIdTable("suspensions", "suspensionId") {
    val userId = reference("userId", User)
    val moderatorId = reference("moderatorId", User)
    val createdAt = timestamp("createdAt")
    val expireAt = timestamp("expireAt")

    val reason = text("reason").nullable()
    val type = postgresEnumeration<SuspensionType>("type", "suspendType")

    val revokedAt = timestamp("revokedAt").nullable()
}

data class SuspensionsDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<SuspensionsDao>(Suspensions)
    val userId by Suspensions.userId
    val user by UserDao referencedOn Suspensions.userId

    val moderatorId by Suspensions.moderatorId
    val moderator by UserDao referencedOn Suspensions.moderatorId

    val createdAt by Suspensions.createdAt
    val expireAt by Suspensions.expireAt
    val revokedAt by Suspensions.revokedAt

    val reason by Suspensions.reason
    val type by Suspensions.type
}
