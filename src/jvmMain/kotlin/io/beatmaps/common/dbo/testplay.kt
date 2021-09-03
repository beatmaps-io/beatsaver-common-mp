package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Testplay : IntIdTable("testplay", "testplayId") {
    val versionId = reference("versionId", Versions)
    val userId = reference("userId", User)

    val feedback = text("feedback").nullable()
    val video = varchar("video", 255).nullable()

    val createdAt = timestamp("createdAt")
    val feedbackAt = timestamp("feedbackAt").nullable()

    fun joinUploader() = join(User, JoinType.INNER, onColumn = userId, otherColumn = User.id)
}

data class TestplayDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<TestplayDao>(Testplay)
    val versionId: EntityID<Int> by Testplay.versionId
    val version: VersionsDao by VersionsDao referencedOn Testplay.versionId

    val userId: EntityID<Int> by Testplay.userId
    val user: UserDao by UserDao referencedOn Testplay.userId

    val feedback: String? by Testplay.feedback
    val video: String? by Testplay.video

    val createdAt: Instant by Testplay.createdAt
    val feedbackAt: Instant? by Testplay.feedbackAt
}
