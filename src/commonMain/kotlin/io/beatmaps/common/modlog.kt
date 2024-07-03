package io.beatmaps.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
sealed interface IModLogOpAction

@Serializable
@SerialName("InfoEdit")
data class InfoEditData(val oldTitle: String, val oldDescription: String, val newTitle: String, val newDescription: String, val oldTags: List<MapTag>? = null, val newTags: List<MapTag>? = null) : IModLogOpAction

@Serializable
@SerialName("Deleted")
data class DeletedData(val reason: String) : IModLogOpAction

@Serializable
@SerialName("Unpublish")
data class UnpublishData(val reason: String) : IModLogOpAction

@Serializable
@SerialName("UploadLimit")
class UploadLimitData(val newValue: Int, val newCurator: Boolean, val verifiedMapper: Boolean? = null, val curatorTab: Boolean? = null) : IModLogOpAction

@Serializable
@SerialName("Suspend")
class SuspendData(val suspended: Boolean, val reason: String? = null) : IModLogOpAction

@Serializable
@SerialName("EditPlaylist")
data class EditPlaylistData(val playlistId: Int, val oldTitle: String, val oldDescription: String, val oldPublic: Boolean, val newTitle: String, val newDescription: String, val newPublic: Boolean) : IModLogOpAction

@Serializable
@SerialName("DeletedPlaylist")
data class DeletedPlaylistData(val playlistId: Int, val reason: String) : IModLogOpAction

@Serializable
@SerialName("ReviewModeration")
data class ReviewModerationData(val oldSentiment: Int, val newSentiment: Int, val oldText: String, val newText: String) : IModLogOpAction

@Serializable
@SerialName("ReviewDelete")
data class ReviewDeleteData(val reason: String, val text: String? = null, val sentiment: Int? = null) : IModLogOpAction

@Serializable
@SerialName("ReplyModeration")
data class ReplyModerationData(val oldText: String, val newText: String) : IModLogOpAction

@Serializable
@SerialName("ReplyDelete")
data class ReplyDeleteData(val reason: String, val text: String? = null) : IModLogOpAction

@Serializable
@SerialName("RevokeSessions")
data class RevokeSessionsData(val all: Boolean, val reason: String? = null) : IModLogOpAction

enum class ModLogOpType(val actionClass: KClass<*>) {
    InfoEdit(InfoEditData::class), Delete(DeletedData::class), Unpublish(UnpublishData::class), UploadLimit(UploadLimitData::class),
    Suspend(SuspendData::class), EditPlaylist(EditPlaylistData::class), DeletedPlaylist(DeletedPlaylistData::class), ReviewModeration(ReviewModerationData::class),
    ReviewDelete(ReviewDeleteData::class), RevokeSessions(RevokeSessionsData::class), ReplyModeration(ReplyModerationData::class), ReplyDelete(ReplyDeleteData::class);

    companion object {
        private val map = entries.associateBy(ModLogOpType::actionClass)
        private val nameMap = entries.associateBy { it.name.lowercase() }
        fun fromAction(action: IModLogOpAction) = map[action::class]
        fun fromName(name: String) = nameMap[name.lowercase()]
    }
}
