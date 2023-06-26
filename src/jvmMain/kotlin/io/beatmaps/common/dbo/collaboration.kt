package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Index

object Collaboration : IntIdTable("collaboration", "collaborationId") {
    val mapId = reference("mapId", Beatmap)
    val collaboratorId = reference("collaboratorId", User)
    val accepted = bool("accepted")

    val link = Index(listOf(mapId, collaboratorId), true, "collaborationLink")
}
