package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.javatime.timestamp
import org.postgresql.util.PGobject

class CiTextColumn : TextColumnType() {
    override fun valueFromDB(value: Any): Any {
        if (value is PGobject && value.type == "citext") {
            return value.value
        }
        return super.valueFromDB(value)
    }

    override fun valueToDB(value: Any?): Any? {
        if (value is String) {
            return PGobject().also {
                it.type = "citext"
                it.value = value
            }
        }
        return super.valueToDB(value)
    }
}

fun Table.citext(name: String): Column<String> = registerColumn(name, CiTextColumn())

val curatorAlias = User.alias("curator")
object User : IntIdTable("uploader", "id") {
    val hash = char("hash", 24).uniqueIndex("hash").nullable()
    val name = text("name")
    val avatar = text("avatar").nullable()
    val email = citext("email").uniqueIndex("email_idx").nullable()
    val steamId = long("steamId").nullable()
    val oculusId = long("oculusId").nullable()
    val discordId = long("discordId").nullable()
    val testplay = bool("testplay")
    val admin = bool("admin")
    val uploadLimit = integer("uploadLimit")
    val upvotes = integer("upvotes")
    val password = char("password", 60).nullable()
    val verifyToken = char("verifyToken", 40).nullable()
    val active = bool("active")
    val uniqueName = citext("uniqueName").nullable()
    val createdAt = timestamp("createdAt")
    val renamedAt = timestamp("renamedAt")
}

data class UserDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<UserDao>(User)
    val name: String by User.name
    val hash: String? by User.hash
    val avatar: String? by User.avatar
    val email: String? by User.email
    val steamId: Long? by User.steamId
    val oculusId: Long? by User.oculusId
    val discordId: Long? by User.discordId
    val testplay: Boolean by User.testplay
    val admin: Boolean by User.admin
    val uploadLimit: Int by User.uploadLimit
    val upvotes by User.upvotes
    val password by User.password
    val verifyToken by User.verifyToken
    val active by User.active
    val uniqueName by User.uniqueName
    val createdAt by User.createdAt
}
