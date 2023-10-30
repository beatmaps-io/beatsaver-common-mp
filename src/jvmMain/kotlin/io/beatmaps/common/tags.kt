package io.beatmaps.common

import io.beatmaps.common.db.contains
import io.beatmaps.common.dbo.Beatmap
import org.jetbrains.exposed.sql.AndOp
import org.jetbrains.exposed.sql.OrOp
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or

fun MapTagQuery.applyToQuery() = AndOp(
    flatMap { x ->
        x.groupBy { it.first }.map { y ->
            val ops = y.value.map { t ->
                val tag = arrayOf(t.second.slug)
                if (y.key) {
                    Beatmap.tags contains tag
                } else {
                    Beatmap.tags.isNull() or not(Beatmap.tags contains tag)
                }
            }

            if (y.key) OrOp(ops) else AndOp(ops)
        }
    }
)
