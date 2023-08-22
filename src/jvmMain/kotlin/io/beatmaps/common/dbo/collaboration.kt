package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.javatime.timestamp

object Collaboration : IntIdTable("collaboration", "collaborationId") {
    val mapId = reference("mapId", Beatmap)
    val collaboratorId = reference("collaboratorId", User)
    val requestedAt = timestamp("requestedAt")
    val accepted = bool("accepted")
    val uploadedAt = timestamp("uploadedAt").nullable()

    val link = Index(listOf(mapId, collaboratorId), true, "collaborationLink")
}

data class CollaborationDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<CollaborationDao>(Collaboration)
    val mapId by Collaboration.mapId
    val collaboratorId by Collaboration.collaboratorId
    val requestedAt by Collaboration.requestedAt
    val accepted by Collaboration.accepted
}
