package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.javatime.timestamp
import org.postgresql.util.PGobject

class CiTextColumn : TextColumnType() {
    override fun valueFromDB(value: Any): String {
        if (value is PGobject && value.type == "citext") {
            return value.value ?: ""
        }
        return super.valueFromDB(value)
    }

    override fun valueToDB(value: String?) =
        PGobject().also {
            it.type = "citext"
            it.value = value
        }
}

fun Table.citext(name: String): Column<String> = registerColumn(name, CiTextColumn())

val curatorAlias by lazy { User.alias("curator") }
val reviewerAlias by lazy { User.alias("reviewer") }
val collaboratorAlias by lazy { User.alias("collaborator") }
object User : IntIdTable("uploader", "id") {
    val hash = char("hash", 24).uniqueIndex("hash").nullable()
    val name = text("name")
    val description = text("description")
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
    val curator = bool("curator")
    val seniorCurator = bool("seniorCurator")
    val curatorTab = bool("curatorTab")
    val verifiedMapper = bool("verifiedMapper")
    val suspendedAt = timestamp("suspendedAt").nullable()
    val bookmarksId = reference("bookmarksId", Playlist).nullable()
    val emailChangedAt = timestamp("emailChangedAt")
    val patreonId = reference("patreonId", Patreon).nullable()
    val curationAlerts = bool("curationAlerts")
    val reviewAlerts = bool("reviewAlerts")
    val followAlerts = bool("followAlerts")

    val updatedAt = timestamp("updatedAt")
    val statsUpdatedAt = timestamp("statsUpdatedAt")
}

data class UserDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<UserDao>(User)
    val name: String by User.name
    val description: String by User.description
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
    val curator by User.curator
    val seniorCurator by User.seniorCurator
    val curatorTab by User.curatorTab
    val verifiedMapper by User.verifiedMapper
    val suspendedAt by User.suspendedAt
    val bookmarksId by User.bookmarksId
    val emailChangedAt by User.emailChangedAt

    val patreon by PatreonDao optionalReferencedOn User.patreonId

    val curationAlerts by User.curationAlerts
    val reviewAlerts by User.reviewAlerts
    val followAlerts by User.followAlerts
}

fun ColumnSet.joinPatreon() = join(Patreon, JoinType.LEFT, onColumn = User.patreonId, otherColumn = Patreon.id)
