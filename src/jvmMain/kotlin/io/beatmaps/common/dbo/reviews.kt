package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Review : IntIdTable("review", "reviewId") {
    val userId = reference("creator", User)
    val mapId = reference("mapId", Beatmap)
    val text = text("text")
    val sentiment = integer("sentiment")
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val curatedAt = timestamp("curatedAt").nullable()
    val deletedAt = timestamp("deletedAt").nullable()
}

data class ReviewDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<ReviewDao>(Review)
    val userId: EntityID<Int> by Review.userId
    val user by UserDao referencedOn Review.userId
    val mapId: EntityID<Int> by Review.mapId
    val map by BeatmapDao referencedOn Review.mapId
    val text by Review.text
    val sentiment by Review.sentiment
    val createdAt by Review.createdAt
    val updatedAt by Review.updatedAt
    val curatedAt by Review.curatedAt
    val deletedAt by Review.deletedAt
}
