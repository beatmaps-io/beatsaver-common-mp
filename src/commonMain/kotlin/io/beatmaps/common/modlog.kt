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
data class InfoEditData(val oldTitle: String, val oldDescription: String, val newTitle: String, val newDescription: String, val oldTags: List<String>? = null, val newTags: List<String>? = null) : IModLogOpAction

@Serializable
@SerialName("Deleted")
data class DeletedData(val reason: String) : IModLogOpAction

@Serializable
@SerialName("Unpublish")
data class UnpublishData(val reason: String) : IModLogOpAction

@Serializable
@SerialName("UploadLimit")
class UploadLimitData(val newValue: Int, val newCurator: Boolean) : IModLogOpAction

@Serializable
@SerialName("EditPlaylist")
data class EditPlaylistData(val playlistId: Int, val oldTitle: String, val oldDescription: String, val oldPublic: Boolean, val newTitle: String, val newDescription: String, val newPublic: Boolean) : IModLogOpAction

@Serializable
@SerialName("DeletedPlaylist")
data class DeletedPlaylistData(val playlistId: Int, val reason: String) : IModLogOpAction

enum class ModLogOpType(val actionClass: KClass<*>) {
    InfoEdit(InfoEditData::class), Delete(DeletedData::class), Unpublish(UnpublishData::class), UploadLimit(UploadLimitData::class),
    EditPlaylist(EditPlaylistData::class), DeletedPlaylist(DeletedPlaylistData::class);

    companion object {
        private val map = values().associateBy(ModLogOpType::actionClass)
        fun fromAction(action: IModLogOpAction) = map[action::class]
    }
}

fun SerializersModuleBuilder.modlog() = polymorphic(IModLogOpAction::class) {
    subclass(InfoEditData::class)
    subclass(DeletedData::class)
    subclass(UnpublishData::class)
    subclass(UploadLimitData::class)
    subclass(EditPlaylistData::class)
    subclass(DeletedPlaylistData::class)
}
