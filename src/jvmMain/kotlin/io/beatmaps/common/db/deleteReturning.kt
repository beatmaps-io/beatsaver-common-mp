package io.beatmaps.common.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.ISqlExpressionBuilder
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

class DeleteReturningStatement(
    private val table: Table,
    private val where: Op<Boolean>? = null,
    private val limit: Int? = 0,
    private val returning: Array<out Column<*>>
) : Iterable<ResultRow>, Statement<ResultSet>(StatementType.DELETE, listOf(table)) {
    private val transaction get() = TransactionManager.current()

    override fun PreparedStatementApi.executeInternal(transaction: Transaction): ResultSet = executeQuery()

    private var iterator: Iterator<ResultRow>? = null

    override fun prepareSQL(transaction: Transaction, prepared: Boolean): String = buildString {
        append("DELETE FROM ")
        append(transaction.identity(table))
        if (where != null) {
            append(" WHERE ")
            append(QueryBuilder(true).append(where).toString())
        }
        if (limit != null) {
            append(" LIMIT ")
            append(limit)
        }
        append(" RETURNING ")

        returning.joinTo(this) { transaction.identity(it) }
    }

    override fun arguments(): Iterable<Iterable<Pair<IColumnType<*>, Any?>>> =
        QueryBuilder(true).run {
            where?.toQueryBuilder(this)
            listOf(args)
        }

    fun exec() {
        require(iterator == null) { "already executed" }

        val resultIterator = ResultIterator(transaction.exec(this)!!)
        iterator = if (transaction.db.supportsMultipleResultSets) {
            resultIterator
        } else {
            Iterable { resultIterator }.toList().iterator()
        }
    }

    override fun iterator(): Iterator<ResultRow> =
        iterator ?: throw IllegalStateException("must call exec() first")

    private inner class ResultIterator(val rs: ResultSet) : Iterator<ResultRow> {
        private var hasNext: Boolean? = null

        override operator fun next(): ResultRow {
            if (hasNext == null) hasNext()
            if (hasNext == false) throw NoSuchElementException()
            hasNext = null
            return ResultRow.create(rs, returning.mapIndexed { index, column -> column to index }.toMap())
        }

        override fun hasNext(): Boolean {
            if (hasNext == null) hasNext = rs.next()
            if (hasNext == false) rs.close()
            return hasNext!!
        }
    }

    companion object {
        fun where(
            table: Table,
            op: Op<Boolean>,
            limit: Int? = null,
            vararg returningColumns: Column<*>
        ): DeleteReturningStatement = DeleteReturningStatement(
            table,
            op,
            limit,
            returningColumns
        ).apply {
            exec()
        }
    }
}

fun <T : Table> T.deleteReturningWhere(
    where: T.(ISqlExpressionBuilder) -> Op<Boolean>,
    vararg returningColumns: Column<*>
): DeleteReturningStatement =
    DeleteReturningStatement.where(
        this,
        where(SqlExpressionBuilder),
        null,
        *returningColumns
    )
