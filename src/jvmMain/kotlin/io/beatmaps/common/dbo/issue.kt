package io.beatmaps.common.dbo

import io.beatmaps.common.api.EIssueType
import io.beatmaps.common.api.IDbIssueData
import io.beatmaps.common.db.json
import io.beatmaps.common.db.postgresEnumeration
import io.beatmaps.common.json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Issue : IntIdTable("issue", "issueId") {
    val creator = reference("creator", User)
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val closedAt = timestamp("closedAt").nullable()
    val type = postgresEnumeration<EIssueType>("type", "issueType")
    val data = json<IDbIssueData>("data", json = json)
}

data class IssueDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<IssueDao>(Issue)

    val creator by UserDao referencedOn Issue.creator
    val creatorId by Issue.creator
    val createdAt by Issue.createdAt
    val closedAt by Issue.closedAt
    val type by Issue.type
    val data by Issue.data
}

object IssueComment : IntIdTable("issue_comment", "commentId") {
    val issueId = reference("issueId", Issue)
    val userId = reference("user", User)
    val public = bool("public")
    val text = text("text")
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
    val deletedAt = timestamp("deletedAt")
}

data class IssueCommentDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<IssueCommentDao>(IssueComment)
    val issue by IssueDao referencedOn IssueComment.issueId
    val user by UserDao referencedOn IssueComment.userId
    val public by IssueComment.public
    val text by IssueComment.text
    val createdAt by IssueComment.createdAt
    val updatedAt by IssueComment.updatedAt
    val deletedAt by IssueComment.deletedAt
}
