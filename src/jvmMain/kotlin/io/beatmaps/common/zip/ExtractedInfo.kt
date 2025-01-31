package io.beatmaps.common.zip

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.info.BaseMapInfo
import io.beatmaps.common.beatsaber.info.DifficultyBeatmapInfo
import io.beatmaps.common.beatsaber.map.BSDiff
import io.beatmaps.common.beatsaber.map.BSLights
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

data class ExtractedInfo(
    val allowedFiles: List<String> = listOf(),
    val mapInfo: BaseMapInfo,
    val score: Short,
    val diffs: MutableMap<ECharacteristic, MutableMap<DifficultyBeatmapInfo, BSDiff>> = mutableMapOf(),
    val lights: MutableMap<ECharacteristic, MutableMap<DifficultyBeatmapInfo, BSLights>> = mutableMapOf(),
    var duration: Float = 0f,
    val toHash: ByteArrayOutputStream = ByteArrayOutputStream(),
    val thumbnail: ByteArrayOutputStream = ByteArrayOutputStream(),
    val preview: ByteArrayOutputStream = ByteArrayOutputStream(),
    var songLengthInfo: SongLengthInfo? = null,
    val maxVivify: Long = 0,
    var vivifyAssets: Set<String>? = null,
    var vivifySize: Long = 0,
    val uncompressedSize: Long = 0,
    val md: MessageDigest = MessageDigest.getInstance("SHA1")
) {
    val digest by lazy {
        val fx = "%0" + md.digestLength * 2 + "x"
        String.format(fx, BigInteger(1, md.digest()))
    }
}
