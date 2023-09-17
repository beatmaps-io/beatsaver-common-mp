package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.timestamp

object Patreon : IntIdTable("patreon", "patreonId") {
    val pledge = integer("pledge").nullable()
    val tier = integer("tier").nullable()
    val active = bool("active")
    val expireAt = timestamp("expireAt").nullable()
}

data class PatreonDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<PatreonDao>(Patreon)
    val pledge by Patreon.pledge
    val tier by Patreon.tier
    val active by Patreon.active
    val expireAt by Patreon.expireAt
}

fun Iterable<ResultRow>.handlePatreon() = this.map { row ->
    if (row.hasValue(Patreon.id) && row[User.patreonId] != null) {
        PatreonDao.wrapRow(row)
    }

    row
}

object PatreonLog : IntIdTable("dev.patreon") {
    val type = varchar("type", 255)
    val text = text("txt")

    val time = timestamp("when")
}
