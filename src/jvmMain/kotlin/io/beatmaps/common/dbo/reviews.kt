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

    val replies = mutableMapOf<EntityID<Int>, ReviewReplyDao>()
}

object ReviewReply : IntIdTable("review_reply", "replyId") {
    val reviewId = reference("reviewId", Review)
    val userId = reference("userId", User)
    val text = text("text")
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val deletedAt = timestamp("deletedAt").nullable()
}

data class ReviewReplyDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<ReviewReplyDao>(ReviewReply)
    val review by ReviewDao referencedOn ReviewReply.reviewId
    val user by UserDao referencedOn  ReviewReply.userId
    val text by ReviewReply.text
    val createdAt by ReviewReply.createdAt
    val updatedAt by ReviewReply.updatedAt
    val deletedAt by ReviewReply.deletedAt
}
