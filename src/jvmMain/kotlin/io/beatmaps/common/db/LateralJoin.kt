package io.beatmaps.common.db

import org.jetbrains.exposed.sql.AbstractQuery
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import java.lang.reflect.Field

fun Query.lateral() = LateralAlias(this)

class LateralAlias(val query: Query) : AbstractQuery<Query>(query.targets) {
    companion object {
        val stringBuilderInQueryBuilder: Field = QueryBuilder::class.java.getDeclaredField("internalBuilder").also {
            it.isAccessible = true
        }
    }

    override val queryToExecute = query
    override val set = query.set

    override fun copy() = query.copy()
    override fun count() = query.count()

    override fun empty() = query.empty()

    override fun prepareSQL(builder: QueryBuilder): String {
        builder {
            // Dirty hack to remove ( from alias
            val strBuilder = stringBuilderInQueryBuilder.get(builder) as StringBuilder
            strBuilder.deleteCharAt(strBuilder.length - 1)

            append("LATERAL (")
            query.prepareSQL(builder)
        }
        return builder.toString()
    }

    override fun withDistinct(value: Boolean) = query.withDistinct(value)

    override fun PreparedStatementApi.executeInternal(transaction: Transaction) =
        with(query) {
            this@executeInternal.executeInternal(transaction)
        }
}
