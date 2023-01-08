package io.beatmaps.common.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.GreaterEqOp
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.IsNullOp
import org.jetbrains.exposed.sql.LessEqOp
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.floatParam
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.math.BigDecimal
import java.time.Instant

fun incrementBy(column: Column<Int>, num: Int = 1) = object : Expression<Int>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("${TransactionManager.current().identity(column)} + $num")
    }
}

infix fun ExpressionWithColumnType<BigDecimal>.greaterEqF(t: Float) = GreaterEqOp(this, floatParam(t))
infix fun ExpressionWithColumnType<BigDecimal>.lessEqF(t: Float) = LessEqOp(this, floatParam(t))

class SimilarOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "<%")
class ArrayContainsOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@>")

infix fun ExpressionWithColumnType<String>.similar(t: String?): Op<Boolean> {
    return if (t == null) {
        IsNullOp(this)
    } else {
        SimilarOp(QueryParameter(t, columnType), this)
    }
}

infix fun <T, S> ExpressionWithColumnType<T>.contains(arry: Array<in S>): Op<Boolean> = ArrayContainsOp(this, QueryParameter(arry, columnType))
infix fun ExpressionWithColumnType<String>.similar(t: ExpressionWithColumnType<String>) = SimilarOp(t, this)

fun unaccent(str: String) = unaccent(QueryParameter(str, TextColumnType()))
fun unaccentLiteral(str: String) = unaccent(stringLiteral(str))
fun unaccent(str: Expression<String>) = CustomFunction<String>("bs_unaccent", TextColumnType(), str)
private val wildcardChar = stringLiteral("%")
fun <T> wildcard(exp: Expression<T>) = PgConcat(null, wildcardChar, exp, wildcardChar)

class PgConcat(
    /** Returns the delimiter. */
    val separator: String?,
    /** Returns the expressions being concatenated. */
    vararg val expr: Expression<*>
) : Function<String>(VarCharColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
        append("(")
        expr.forEachIndexed { idx, it ->
            if (idx > 0) append(if (separator == null) " || " else " || '$separator' || ")
            append(it)
        }
        append(")")
    }
}

class InsensitiveLikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")
infix fun <T : String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> = InsensitiveLikeOp(this, QueryParameter(pattern, columnType))
infix fun <T : String?> ExpressionWithColumnType<T>.ilike(exp: ExpressionWithColumnType<T>): Op<Boolean> = InsensitiveLikeOp(this, exp)

fun <T : Any> isFalse(query: Op<T>) = object : Expression<T>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("(")
        append(query)
        append(") IS FALSE")
    }
}

fun <T : Any> wrapAsExpressionNotNull(query: Query) = object : Expression<T>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("(")
        query.prepareSQL(this)
        append(")")
    }
}

fun <T : Any> wrapAsExpressionNotNull(query: Query, columnType: IColumnType) = object : ExpressionWithColumnType<T>() {
    override val columnType = columnType

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("(")
        query.prepareSQL(this)
        append(")")
    }
}

fun <T> wrapAsOp(expr: Expression<T>) = object : Op<T>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        expr.toQueryBuilder(queryBuilder)
    }
}

fun countAsInt(expr: Expression<*>): Expression<Int> = object : Function<Int>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"COUNT("
        +expr
        +")"
    }
}

fun countWithFilter(condition: Expression<Boolean>): Expression<Int> = object : Function<Int>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"COUNT(*) FILTER (WHERE "
        +condition
        +")"
    }
}

fun <T> Expression<T>.countWithFilter(condition: Expression<Boolean>): Expression<Int> = object : Function<Int>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"COUNT("
        +this@countWithFilter
        +") FILTER (WHERE "
        +condition
        +")"
    }
}

fun <T> Expression<T>.avgWithFilter(condition: Expression<Boolean>, scale: Int = 2): Expression<BigDecimal?> = object : Function<BigDecimal?>(DecimalColumnType(Int.MAX_VALUE, scale)) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"AVG("
        +this@avgWithFilter
        +") FILTER (WHERE "
        +condition
        +")"
    }
}

operator fun Expression<*>.minus(other: Expression<Instant?>) = object : Function<Int>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"DATE_PART('days', "
        +this@minus
        +" - "
        +other
        +")"
    }
}

fun <T> Column<T>.distinctOn(vararg columns: Expression<*>): Function<T> = DistinctOn(this, columns)

class DistinctOn<T>(private val expr: Column<T>, private val columns: Array<out Expression<*>>) : Function<T>(expr.columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"DISTINCT ON ("
        columns.forEachIndexed { idx, it ->
            if (idx > 0) append(", ")
            +it
        }
        +")"
        +expr
    }
}

class DateMinusDays(val dateExp: Expression<Instant>, val d: Int) : Expression<Instant>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +dateExp
        +" - INTERVAL '$d DAYS'"
    }
}
