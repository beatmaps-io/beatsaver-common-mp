package io.beatmaps.common.db

import io.beatmaps.common.api.searchEnum
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.postgresql.ds.PGSimpleDataSource
import org.postgresql.util.PGobject
import javax.sql.DataSource

fun setupDB(defaultDb: String = "beatmaps"): DataSource {
    val dbHost = System.getenv("DB_HOST") ?: ""
    val dbPort = System.getenv("DB_PORT") ?: "5432"
    val dbUser = System.getenv("DB_USER") ?: "beatmaps"
    val dbName = System.getenv("DB_NAME") ?: defaultDb
    val dbPass = System.getenv("DB_PASSWORD") ?: "insecure-password"

    return PGSimpleDataSource().also {
        it.serverName = dbHost
        it.portNumber = dbPort.toInt()
        it.databaseName = dbName
        it.user = dbUser
        it.password = dbPass
    }.also {
        Database.connect(it)
    }
}

inline fun <reified T : Enum<T>> Table.postgresEnumeration(
    columnName: String,
    postgresEnumName: String
) = customEnumeration(
    columnName, postgresEnumName,
    { value -> searchEnum<T>(value as String) }, { PGEnum(postgresEnumName, it) }
)

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name?.removePrefix("_")
        type = enumTypeName
    }
}
