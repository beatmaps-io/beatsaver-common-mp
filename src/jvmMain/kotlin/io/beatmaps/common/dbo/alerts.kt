package io.beatmaps.common.dbo

import io.beatmaps.common.api.EAlertType
import io.beatmaps.common.db.NowExpression
import io.beatmaps.common.db.postgresEnumeration
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp

object Alert: IntIdTable("alert", "alertId") {
    val head = varchar("head", 255)
    val body = text("body")
    val type = postgresEnumeration<EAlertType>("type", "alertType")

    val sentAt = timestamp("sentAt")

    fun insert(alertHead: String, alertBody: String, alertType: EAlertType, recipientIds: List<Int>) {
        val newAlert = insert {
            it[head] = alertHead
            it[body] = alertBody
            it[type] = alertType
            it[sentAt] = NowExpression(sentAt.columnType)
        }.resultedValues?.first()

        newAlert?.let { a ->
            val alert = AlertDao.wrapRow(a)
            recipientIds.forEach { id ->
                AlertRecipient.insert {
                    it[recipientId] = id
                    it[alertId] = alert.id
                }
            }
        }
    }

    fun insert(alertHead: String, alertBody: String, alertType: EAlertType, recipientId: Int) =
        insert(alertHead, alertBody, alertType, listOf(recipientId))
}

data class AlertDao(val key: EntityID<Int>): IntEntity(key) {
    companion object: IntEntityClass<AlertDao>(Alert)
    val head by Alert.head
    val body by Alert.body
    val type by Alert.type

    val sentAt by Alert.sentAt
}

object AlertRecipient: IntIdTable("alert_recipient", "id") {
    val recipientId = reference("recipientId", User)
    val alertId = reference("alertId", Alert)

    val readAt = timestamp("readAt").nullable()

    val link = Index(listOf(alertId, recipientId), true, "alertLink")
}

data class AlertRecipientDao(val key: EntityID<Int>): IntEntity(key) {
    companion object: IntEntityClass<AlertRecipientDao>(AlertRecipient)

    val recipient by UserDao referencedOn AlertRecipient.recipientId
    val alert by AlertDao referencedOn AlertRecipient.alertId

    val readAt by AlertRecipient.readAt
}
