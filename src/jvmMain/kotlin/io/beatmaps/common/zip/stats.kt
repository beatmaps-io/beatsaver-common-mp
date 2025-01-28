package io.beatmaps.common.zip

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.info.BaseMapInfo
import io.beatmaps.common.beatsaber.info.DifficultyBeatmap
import io.beatmaps.common.beatsaber.info.DifficultyBeatmapInfo
import io.beatmaps.common.beatsaber.info.DifficultyBeatmapSet
import io.beatmaps.common.beatsaber.info.MapInfo
import io.beatmaps.common.beatsaber.map.BSDiff
import io.beatmaps.common.beatsaber.map.BSLights
import io.beatmaps.common.checkParity
import io.beatmaps.common.dbo.Difficulty
import io.beatmaps.common.dbo.Versions
import io.beatmaps.common.dbo.VersionsDao
import io.beatmaps.common.dbo.maxAllowedNps
import io.beatmaps.common.or
import io.beatmaps.common.pow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.lang.Float.min
import java.math.BigDecimal

fun ZipHelper.parseDifficulty(hash: String, diff: DifficultyBeatmap, char: DifficultyBeatmapSet, map: MapInfo, sli: SongLengthInfo, ver: VersionsDao? = null) {
    val version = ver ?: VersionsDao.wrapRow(
        Versions.selectAll().where {
            Versions.hash eq hash
        }.first()
    )

    Difficulty.insertIgnore {
        it[mapId] = version.mapId
        it[versionId] = version.id
        it[createdAt] = version.uploaded

        val bsdiff = diff(diff.beatmapFilename.or(""))
        if (bsdiff !is BSLights) throw Exception("Wrong beatmap type")

        sharedInsert(it, char.enumValue(), diff, bsdiff, bsdiff, map, sli)
        it[characteristic] = char.enumValue()
        it[difficulty] = diff.enumValue()
    }
}

fun Difficulty.sharedInsert(it: UpdateBuilder<*>, characteristic: ECharacteristic, diff: DifficultyBeatmapInfo, bsdiff: BSDiff, bslights: BSLights?, map: BaseMapInfo, sli: SongLengthInfo) {
    it[njs] = diff.noteJumpMovementSpeed.or(0f)
    it[offset] = diff.noteJumpStartBeatOffset.or(0f)

    checkParity(bsdiff).also { pr ->
        it[pReset] = pr.info
        it[pError] = pr.errors
        it[pWarn] = pr.warnings
    }

    val maxLen = 10.pow(7) - 1.0f

    val len = bsdiff.songLength()
    val noteCount = bsdiff.noteCount()
    val mappedNps = bsdiff.mappedNps(sli)
    val bpm = map.getBpm() ?: 0f

    it[schemaVersion] = bsdiff.version.or("2.2.0")
    it[notes] = noteCount
    it[bombs] = bsdiff.bombCount()
    it[arcs] = bsdiff.arcCount()
    it[chains] = bsdiff.chainCount()
    it[obstacles] = bsdiff.obstacleCount()
    it[events] = bslights?.eventCount() ?: 0
    it[length] = min(len, maxLen).toBigDecimal()
    it[seconds] = min(if (bpm == 0f) 0f else (60 / bpm) * len, maxLen).toBigDecimal()
    it[maxScore] = bsdiff.maxScore()
    it[label] = diff.customData.orNull()?.difficultyLabel?.orNull()?.take(255)

    val environmentIndex = diff.environmentIndex.or(-1)
    it[environment] = map.getEnvironment(environmentIndex, characteristic.rotation)

    it[nps] = BigDecimal.valueOf(mappedNps.toDouble()).min(maxAllowedNps)

    it[requirements] = diff.customData.orNull()?.requirements?.orNull()?.mapNotNull { it.orNull() }
    it[suggestions] = diff.customData.orNull()?.suggestions?.orNull()?.mapNotNull { it.orNull() }
    it[information] = diff.customData.orNull()?.information?.orNull()?.mapNotNull { it.orNull() }
    it[warnings] = diff.customData.orNull()?.warnings?.orNull()?.mapNotNull { it.orNull() }
}
