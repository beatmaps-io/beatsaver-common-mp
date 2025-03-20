package io.beatmaps.common

import java.io.File

object Folders {
    enum class StorageItem(val envName: String, val defaultFolder: String) {
        AVATAR("AVATAR_DIR", "avatars"),
        ZIP("ZIP_DIR", "zips"),
        COVER("COVER_DIR", "covers"),
        AUDIO("AUDIO_DIR", "audio"),
        PLAYLIST_COVER("PLAYLIST_COVER_DIR", "playlists"),
        UPLOAD("UPLOAD_DIR", "uploads")
    }

    private val envVals = StorageItem.entries.associateWith<StorageItem, String?> { System.getenv(it.envName) }

    private fun File.createFolder() = this.also {
        if (!it.exists()) it.mkdir()
    }
    private fun defaultBaseFolder() = File("data").createFolder()

    private val folderCache = mutableMapOf<StorageItem, File>()
    private fun getFolder(si: StorageItem, create: Boolean = true) = folderCache.getOrPut(si) {
        envVals[si]?.let { File(it) } ?: File(defaultBaseFolder(), si.defaultFolder).also { dir ->
            if (create) dir.createFolder()
        }
    }

    private val localFolders = mutableMapOf<Pair<StorageItem, String>, File>()
    private fun getSubFolder(si: StorageItem, c: Char, create: Boolean = true) = getSubFolder(si, c.toString(), create)
    private fun getSubFolder(si: StorageItem, c: String, create: Boolean) = localFolders.getOrPut(si to c) {
        File(getFolder(si), c).also { dir ->
            if (create) dir.createFolder()
        }
    }

    fun uploadTempFolder() = getFolder(StorageItem.UPLOAD)
    fun localAvatarFolder() = getFolder(StorageItem.AVATAR)
    fun localFolder(hash: String, create: Boolean = true) = getSubFolder(StorageItem.ZIP, hash[0], create)
    fun localCoverFolder(hash: String, create: Boolean = true) = getSubFolder(StorageItem.COVER, hash[0], create)
    fun localAudioFolder(hash: String, create: Boolean = true) = getSubFolder(StorageItem.AUDIO, hash[0], create)

    fun localPlaylistCoverFolder(size: Int = 256, create: Boolean = true) = if (size != 256) {
        getSubFolder(StorageItem.PLAYLIST_COVER, "$size", create)
    } else {
        getFolder(StorageItem.PLAYLIST_COVER, create)
    }
}
