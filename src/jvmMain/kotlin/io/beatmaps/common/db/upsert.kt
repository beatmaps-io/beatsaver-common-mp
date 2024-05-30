package io.beatmaps.common.db

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import java.sql.ResultSet

interface ConflictType {
    fun StringBuilder.prepareSQL()
    fun shouldUpdate(column: Column<*>): Boolean
    val returning: List<Column<*>>
}

class IndexConflict(val index: Index) : ConflictType {
    override fun StringBuilder.prepareSQL() {
        append(" ON CONFLICT ON CONSTRAINT \"")
        append(index.indexName)
        append("\"")
    }

    override fun shouldUpdate(column: Column<*>) = column !in index.columns

    override val returning: List<Column<*>>
        get() = listOf(index.columns.first())
}

class ColumnConflict(val column: Column<*>) : ConflictType {
    override fun StringBuilder.prepareSQL() {
        append(" ON CONFLICT(\"")
        append(column.name)
        append("\")")
    }

    override fun shouldUpdate(column: Column<*>) = column != this.column

    override val returning: List<Column<*>>
        get() = listOf(column)
}

class UpsertStatement<Key : Any>(table: Table, private val conflict: ConflictType, val update: Boolean = true) :
    InsertStatement<Key>(table, false) {

    override fun prepareSQL(transaction: Transaction, prepared: Boolean) = buildString {
        if (argumentsCache != null) {
            arguments = argumentsCache
        }

        append(super.prepareSQL(transaction, prepared))

        val dialect = transaction.db.dialect
        if (dialect is PostgreSQLDialect) {
            with(conflict) {
                prepareSQL()
            }

            if (update) {
                append(" DO UPDATE SET ")

                values.keys.filter(conflict::shouldUpdate)
                    .joinTo(this) { "${transaction.identity(it)}=EXCLUDED.${transaction.identity(it)}" }
            } else {
                append(" DO NOTHING")

                if (conflict.returning.isNotEmpty()) {
                    conflict.returning.joinTo(this, ", ", " RETURNING ") { transaction.identity(it) }
                }
            }
        } else {
            append(" ON DUPLICATE KEY UPDATE ")
            values.keys.filter(conflict::shouldUpdate)
                .joinTo(this) { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        }
    }

    private var argumentsCache: List<List<Pair<Column<*>, Any?>>>? = null

    override fun PreparedStatementApi.execInsertFunction(): Pair<Int, ResultSet?> {
        val inserted = if (arguments().count() > 1 || isAlwaysBatch) executeBatch().count() else executeUpdate()
        argumentsCache = arguments
        arguments = listOf()
        val rs = if (autoIncColumns.isNotEmpty()) {
            resultSet
        } else { null }
        return inserted to rs
    }
}

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.upsertCustom(
    conflict: ConflictType,
    update: Boolean = true,
    body: T.(UpsertStatement<Number>) -> Unit
) =
    UpsertStatement<Number>(this, conflict, update).apply {
        body(this)
        execute(TransactionManager.current())
    }

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.upsert(
    conflictColumn: Column<*>? = null,
    conflictIndex: Index? = null,
    body: T.(UpsertStatement<Number>) -> Unit
) = when {
    conflictColumn != null -> ColumnConflict(conflictColumn)
    conflictIndex != null -> IndexConflict(conflictIndex)
    else -> throw IllegalArgumentException()
}.let {
    with(upsertCustom(it, true, body)) {
        get(id)
    }
}

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.insertIgnoreReturning(
    returning: Column<*>,
    conflictIndex: Index,
    body: T.(UpsertStatement<Number>) -> Unit
) = upsertCustom(
    IndexConflict(Index(listOf(returning), conflictIndex.unique, conflictIndex.customName)),
    false,
    body
)

fun Table.indexR(customIndexName: String? = null, isUnique: Boolean = false, vararg columns: Column<*>): Index {
    val index = Index(columns.toList(), isUnique, customIndexName)
    (indices as MutableList).add(index)
    return index
}

fun Table.uniqueIndexR(customIndexName: String? = null, vararg columns: Column<*>): Index =
    indexR(customIndexName, true, *columns)
