package io.beatmaps.common.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet
import java.util.LinkedHashMap
import java.util.NoSuchElementException

class UpdateReturningStatement(
    private val table: Table,
    private val returningColumns: Array<out Column<*>>,
    private val limit: Int? = null,
    private val where: Op<Boolean>? = null
) : Statement<List<ResultRow>>(StatementType.SELECT, listOf(table)) {

    private val values: MutableMap<Column<*>, Any?> = LinkedHashMap()

    operator fun <S> set(column: Column<S>, value: S) {
        when {
            values.containsKey(column) -> error("$column is already initialized")
            !column.columnType.nullable && value == null -> error("Trying to set null to not nullable column $column")
            else -> values[column] = value
        }
    }

    @JvmName("setWithEntityIdExpression")
    operator fun <S, ID : EntityID<S>, E : Expression<S>> set(column: Column<ID>, value: E) {
        require(!values.containsKey(column)) { "$column is already initialized" }
        values[column] = value
    }

    @JvmName("setWithEntityIdValue")
    operator fun <S : Comparable<S>, ID : EntityID<S>, E : S?> set(column: Column<ID>, value: E) {
        require(!values.containsKey(column)) { "$column is already initialized" }
        values[column] = value
    }

    operator fun <T, S : T, E : Expression<S>> set(column: Column<T>, value: E) {
        require(!values.containsKey(column)) { "$column is already initialized" }
        values[column] = value
    }

    private fun parentPrepareSQL(transaction: Transaction) = transaction.db.dialect.functionProvider.update(table, values.toList(), limit, where, transaction)

    override fun prepareSQL(transaction: Transaction) = buildString {
        append(parentPrepareSQL(transaction))

        val dialect = transaction.db.vendor
        if (dialect == "postgresql") {
            append(" RETURNING ")

            returningColumns.joinTo(this) { transaction.identity(it) }
        }
    }

    override fun arguments(): Iterable<Iterable<Pair<IColumnType, Any?>>> = QueryBuilder(true).run {
        values.forEach {
            registerArgument(it.key, it.value)
        }
        where?.toQueryBuilder(this)
        if (args.isNotEmpty()) listOf(args) else emptyList()
    }

    override fun PreparedStatementApi.executeInternal(transaction: Transaction): List<ResultRow>? {
        if (values.isEmpty()) return null
        return ResultIterator(executeQuery()).asSequence().toList()
    }

    inner class ResultIterator(private val rs: ResultSet) : Iterator<ResultRow> {
        private var hasNext: Boolean? = null

        override operator fun next(): ResultRow {
            if (hasNext == null) hasNext()
            if (hasNext == false) throw NoSuchElementException()
            hasNext = null
            return ResultRow.create(rs, returningColumns.mapIndexed { index, column -> column to index }.toMap())
        }

        override fun hasNext(): Boolean {
            if (hasNext == null) hasNext = rs.next()
            if (hasNext == false) rs.close()
            return hasNext!!
        }
    }
}

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.updateReturning(
    where: SqlExpressionBuilder.() -> Op<Boolean>,
    body: T.(UpdateReturningStatement) -> Unit,
    vararg returningColumns: Column<*>
) =
    UpdateReturningStatement(this, returningColumns, null, SqlExpressionBuilder.where()).run {
        body(this)
        execute(TransactionManager.current())
    }

class NowExpression<T>(override val columnType: IColumnType) : ExpressionWithColumnType<T>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("NOW()")
    }
}
