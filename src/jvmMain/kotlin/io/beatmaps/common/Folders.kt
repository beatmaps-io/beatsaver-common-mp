package io.beatmaps.common

import java.io.File

object Folders {
    enum class StorageItem(val envName: String, val defaultFolder: String) {
        AVATAR("AVATAR_DIR", "avatars"),
        ZIP("ZIP_DIR", "zips"),
        COVER("COVER_DIR", "covers"),
        AUDIO("AUDIO_DIR", "audio"),
        PLAYLIST_COVER("PLAYLIST_COVER_DIR", "playlists")
    }

    private val envVals = StorageItem.entries.associateWith<StorageItem, String?> { System.getenv(it.envName) }

    private fun File.createFolder() = this.also {
        if (!it.exists()) it.mkdir()
    }
    private fun defaultBaseFolder() = File("data").createFolder()

    private val folderCache = mutableMapOf<StorageItem, File>()
    private fun getFolder(si: StorageItem) = folderCache.getOrPut(si) {
        val dir = envVals[si]?.let { File(it) } ?: File(defaultBaseFolder(), si.defaultFolder)
        dir.createFolder()
    }

    private val localFolders = mutableMapOf<Pair<StorageItem, String>, File>()
    private fun getSubFolder(si: StorageItem, c: Char) = getSubFolder(si, c.toString())
    private fun getSubFolder(si: StorageItem, c: String) = localFolders.getOrPut(si to c) {
        File(getFolder(si), c).createFolder()
    }

    fun localAvatarFolder() = getFolder(StorageItem.AVATAR)
    fun localFolder(hash: String) = getSubFolder(StorageItem.ZIP, hash[0])
    fun localCoverFolder(hash: String) = getSubFolder(StorageItem.COVER, hash[0])
    fun localAudioFolder(hash: String) = getSubFolder(StorageItem.AUDIO, hash[0])

    fun localPlaylistCoverFolder(size: Int = 256) = if (size != 256) {
        getSubFolder(StorageItem.PLAYLIST_COVER, "$size")
    } else {
        getFolder(StorageItem.PLAYLIST_COVER)
    }
}
