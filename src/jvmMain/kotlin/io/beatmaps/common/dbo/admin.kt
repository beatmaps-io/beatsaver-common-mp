package io.beatmaps.common.dbo

import io.beatmaps.common.IModLogOpAction
import io.beatmaps.common.ModLogOpType
import io.beatmaps.common.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.javatime.timestamp

object ModLog : IntIdTable("modlog", "logId") {
    val opBy = reference("userId", User)
    val opOn = reference("mapId", Beatmap).nullable()
    val targetUser = reference("targetUserId", User)
    val opAt = timestamp("when")
    val type = integer("type")
    val action = text("action")

    fun insert(userId: Int, mapId: Int?, a: IModLogOpAction, targetUserId: Int) = insert(userId, mapId, a, intLiteral(targetUserId))

    fun insert(userId: Int, mapId: Int?, a: IModLogOpAction, targetUserId: Expression<Int>) =
        (ModLogOpType.fromAction(a) ?: throw RuntimeException("Action type not valid")).let { t ->
            insert {
                it[opBy] = userId
                it[opOn] = if (mapId == null) null else EntityID(mapId, Beatmap)
                it[targetUser] = targetUserId
                it[type] = t.ordinal
                it[action] = json.encodeToString(a)
            }
        }
}

data class ModLogDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<ModLogDao>(ModLog)

    val opBy by UserDao referencedOn ModLog.opBy
    val opOn by BeatmapDao optionalReferencedOn ModLog.opOn
    val opAt by ModLog.opAt
    val targetUser by UserDao referencedOn ModLog.targetUser
    private val type by ModLog.type
    private val action by ModLog.action

    fun realType() = ModLogOpType.entries[type]
    fun realAction() = try {
        json.decodeFromString(action)
    } catch (e: Exception) {
        json.decodeFromString(realType().actionClass.serializer(), action) as IModLogOpAction
    }
}
