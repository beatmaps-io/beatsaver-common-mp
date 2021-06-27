package io.beatmaps.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

interface IModLogOpAction

@Serializable
@SerialName("InfoEdit")
data class InfoEditData(val oldTitle: String, val oldDescription: String, val newTitle: String, val newDescription: String) : IModLogOpAction

@Serializable
@SerialName("Deleted")
data class DeletedData(val reason: String) : IModLogOpAction

@Serializable
@SerialName("Unpublish")
data class UnpublishData(val reason: String) : IModLogOpAction

enum class ModLogOpType(val actionClass: KClass<*>) {
    InfoEdit(InfoEditData::class), Delete(DeletedData::class), Unpublish(UnpublishData::class);

    companion object {
        private val map = values().associateBy(ModLogOpType::actionClass)
        fun fromAction(action: IModLogOpAction) = map[action::class]
    }
}

fun SerializersModuleBuilder.modlog() = polymorphic(IModLogOpAction::class) {
    subclass(InfoEditData::class)
    subclass(DeletedData::class)
    subclass(UnpublishData::class)
}