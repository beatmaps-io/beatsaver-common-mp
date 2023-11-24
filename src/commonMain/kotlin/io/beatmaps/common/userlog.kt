package io.beatmaps.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
sealed interface IUserLogOpAction

@Serializable
@SerialName("EmailChanged")
data class EmailChangedData(val oldEmail: String?, val newEmail: String) : IUserLogOpAction

@Serializable
@SerialName("PasswordChanged")
object PasswordChangedData : IUserLogOpAction

enum class UserLogOpType(val actionClass: KClass<*>) {
    EmailChanged(EmailChangedData::class), PasswordChanged(PasswordChangedData::class);

    companion object {
        private val map = values().associateBy(UserLogOpType::actionClass)
        private val nameMap = values().associateBy { it.name.lowercase() }
        fun fromAction(action: IUserLogOpAction) = map[action::class]
        fun fromName(name: String) = nameMap[name.lowercase()]
    }
}
