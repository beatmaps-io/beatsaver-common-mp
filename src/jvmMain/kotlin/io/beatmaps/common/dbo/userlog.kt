package io.beatmaps.common.dbo

import io.beatmaps.common.IUserLogOpAction
import io.beatmaps.common.UserLogOpType
import io.beatmaps.common.db.json
import io.beatmaps.common.json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp

object UserLog : IntIdTable("userlog", "logId") {
    val opBy = reference("userId", User)
    val opOn = reference("mapId", Beatmap).nullable()
    val opAt = timestamp("when")
    val type = integer("type")
    val action = json<IUserLogOpAction>("action", json = json)

    fun insert(userId: Int, mapId: Int?, a: IUserLogOpAction) =
        (UserLogOpType.fromAction(a) ?: throw RuntimeException("Action type not valid")).let { t ->
            insert {
                it[opBy] = userId
                it[opOn] = if (mapId == null) null else EntityID(mapId, Beatmap)
                it[type] = t.ordinal
                it[action] = a
            }
        }
}

data class UserLogDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<UserLogDao>(UserLog)

    val opBy by UserDao referencedOn UserLog.opBy
    val opOn by BeatmapDao optionalReferencedOn UserLog.opOn
    val opAt by UserLog.opAt
    private val type by UserLog.type
    private val action by UserLog.action

    fun realType() = UserLogOpType.values()[type]
}
