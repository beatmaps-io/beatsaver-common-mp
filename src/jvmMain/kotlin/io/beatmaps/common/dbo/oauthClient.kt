package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp

object OauthClient : IntIdTable("oauthClients", "id") {
    val clientId = text("clientId")
    val secret = text("secret")
    val name = text("name")
    val scopes = text("scopes").nullable()
    val redirectUrl = text("redirectUrl").nullable()
    val iconUrl = text("iconUrl").nullable()
}

data class OauthClientDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<OauthClientDao>(OauthClient)
    val clientId: String by OauthClient.clientId
    val secret: String by OauthClient.secret
    val name: String by OauthClient.name

    val scopes: String? by OauthClient.scopes
    val redirectUrl: String? by OauthClient.redirectUrl
    val iconUrl: String? by OauthClient.iconUrl
}

object AccessTokenTable : IdTable<String>("oa_access_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val type = varchar("type", 256)
    val expiration = timestamp("expiration")
    val scope = varchar("scope", 256)
    val userName = integer("user_name").nullable()
    val metadata = text("metadata")
    val clientId = varchar("client_id", 256)
    val refreshToken = varchar("refresh_token", 256).nullable()
}

object RefreshTokenTable : IdTable<String>("oa_refresh_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val expiration = timestamp("expiration")
    val scope = varchar("scope", 256)
    val userName = integer("user_name").nullable()
    val metadata = text("metadata")
    val clientId = varchar("client_id", 256)
}
