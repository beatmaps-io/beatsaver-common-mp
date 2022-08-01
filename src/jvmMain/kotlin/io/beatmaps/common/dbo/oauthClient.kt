package io.beatmaps.common.dbo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

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
