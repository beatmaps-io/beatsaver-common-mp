package io.beatmaps.common.dbo

import io.beatmaps.common.db.deleteReturningWhere
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.not

object Collaboration : IntIdTable("collaboration", "collaborationId") {
    val mapId = reference("mapId", Beatmap)
    val collaboratorId = reference("collaboratorId", User)
    val requestedAt = timestamp("requestedAt")
    val accepted = bool("accepted")

    val link = Index(listOf(mapId, collaboratorId), true, "collaborationLink")

    fun deleteForMap(id: Int) =
        Collaboration.deleteReturningWhere({ mapId eq id and not(accepted) }, collaboratorId).map {
            it[collaboratorId].value
        }
}

data class CollaborationDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<CollaborationDao>(Collaboration)
    val mapId by Collaboration.mapId
    val collaboratorId by Collaboration.collaboratorId
    val requestedAt by Collaboration.requestedAt
    val accepted by Collaboration.accepted
}
