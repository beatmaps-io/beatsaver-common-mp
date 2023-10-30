package io.beatmaps.common

import io.beatmaps.common.db.contains
import io.beatmaps.common.dbo.Beatmap
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or

fun MapTags.applyToQuery() = entries.fold(Op.TRUE as Op<Boolean>) { op, t ->
    op and t.value.entries.fold(Op.FALSE as Op<Boolean>) { op2, t2 ->
        op2 or
            if (!t.key) {
                Beatmap.tags.isNull() or not(Beatmap.tags contains t2.value.toTypedArray())
            } else {
                Beatmap.tags contains t2.value.toTypedArray()
            }
    }
}
