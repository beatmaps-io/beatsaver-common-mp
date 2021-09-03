package io.beatmaps.common.db

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

class UpsertStatement<Key : Any>(table: Table, conflictColumn: Column<*>? = null, conflictIndex: Index? = null, val update: Boolean = true) :
    InsertStatement<Key>(table, false) {

    private val indexName: String
    private val indexColumns: List<Column<*>>
    private val index: Boolean

    init {
        when {
            conflictIndex != null -> {
                index = true
                indexName = conflictIndex.indexName
                indexColumns = conflictIndex.columns
            }
            conflictColumn != null -> {
                index = false
                indexName = conflictColumn.name
                indexColumns = listOf(conflictColumn)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun prepareSQL(transaction: Transaction) = buildString {
        append(super.prepareSQL(transaction))

        val dialect = transaction.db.vendor
        if (dialect == "postgresql") {
            if (index) {
                append(" ON CONFLICT ON CONSTRAINT \"")
                append(indexName)
                append("\"")
            } else {
                append(" ON CONFLICT(\"")
                append(indexName)
                append("\")")
            }

            if (update) {
                append(" DO UPDATE SET ")

                values.keys.filter { it !in indexColumns }
                    .joinTo(this) { "${transaction.identity(it)}=EXCLUDED.${transaction.identity(it)}" }
            } else {
                append(" DO NOTHING RETURNING ${transaction.identity(indexColumns.first())}")
            }
        } else {

            append(" ON DUPLICATE KEY UPDATE ")
            values.keys.filter { it !in indexColumns }
                .joinTo(this) { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        }
    }
}

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.upsert(
    conflictColumn: Column<*>? = null,
    conflictIndex: Index? = null,
    body: T.(UpsertStatement<Number>) -> Unit
) =
    UpsertStatement<Number>(this, conflictColumn, conflictIndex).run {
        body(this)
        execute(TransactionManager.current())
        get(id)
    }

inline fun <T : IdTable<Key>, Key : Comparable<Key>> T.insertIgnoreReturning(
    returning: Column<*>,
    conflictIndex: Index,
    body: T.(UpsertStatement<Number>) -> Unit
) =
    UpsertStatement<Number>(this, null, Index(listOf(returning), conflictIndex.unique, conflictIndex.customName), false).run {
        body(this)
        execute(TransactionManager.current())
        get(returning)
    }

fun Table.indexR(customIndexName: String? = null, isUnique: Boolean = false, vararg columns: Column<*>): Index {
    val index = Index(columns.toList(), isUnique, customIndexName)
    (indices as MutableList).add(index)
    return index
}

fun Table.uniqueIndexR(customIndexName: String? = null, vararg columns: Column<*>): Index =
    indexR(customIndexName, true, *columns)
