package io.beatmaps.common.zip

import io.beatmaps.common.beatsaber.BSDiff
import io.beatmaps.common.beatsaber.BSLights
import io.beatmaps.common.beatsaber.BaseMapInfo
import io.beatmaps.common.beatsaber.DifficultyBeatmap
import io.beatmaps.common.beatsaber.DifficultyBeatmapInfo
import io.beatmaps.common.beatsaber.DifficultyBeatmapSet
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.SongLengthInfo
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

data class DiffStats(val chroma: Boolean, val noodle: Boolean, val me: Boolean, val cinema: Boolean, val nps: BigDecimal)
fun List<String>?.containsIgnoreCase(element: String) = this?.any { e -> e.equals(element, true) } ?: false

fun ZipHelper.parseDifficulty(hash: String, diff: DifficultyBeatmap, char: DifficultyBeatmapSet, map: MapInfo, sli: SongLengthInfo, ver: VersionsDao? = null): DiffStats {
    val version = ver ?: VersionsDao.wrapRow(
        Versions.selectAll().where {
            Versions.hash eq hash
        }.first()
    )

    var stats = DiffStats(chroma = false, noodle = false, me = false, cinema = false, nps = BigDecimal.ZERO)

    Difficulty.insertIgnore {
        it[mapId] = version.mapId
        it[versionId] = version.id
        it[createdAt] = version.uploaded

        val bsdiff = diff(diff.beatmapFilename.or(""))
        if (bsdiff !is BSLights) throw Exception("Wrong beatmap type")

        stats = sharedInsert(it, diff, bsdiff, bsdiff, map, sli)
        it[characteristic] = char.enumValue()
        it[difficulty] = diff.enumValue()
    }

    return stats
}

fun Difficulty.sharedInsert(it: UpdateBuilder<*>, diff: DifficultyBeatmapInfo, bsdiff: BSDiff, bslights: BSLights?, map: BaseMapInfo, sli: SongLengthInfo): DiffStats {
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
    it[environment] = map.getEnvironment(environmentIndex)

    val requirementsLocal = diff.customData.orNull()?.requirements?.orNull()?.mapNotNull { it.orNull() }
    val suggestionsLocal = diff.customData.orNull()?.suggestions?.orNull()?.mapNotNull { it.orNull() }

    return DiffStats(
        requirementsLocal.containsIgnoreCase("Chroma") || suggestionsLocal.containsIgnoreCase("Chroma"),
        requirementsLocal.containsIgnoreCase("Noodle Extensions"),
        requirementsLocal.containsIgnoreCase("Mapping Extensions"),
        requirementsLocal.containsIgnoreCase("Cinema") || suggestionsLocal.containsIgnoreCase("Cinema"),
        BigDecimal.valueOf(mappedNps.toDouble()).min(maxAllowedNps)
    ).also { stats ->
        it[nps] = stats.nps
        it[chroma] = stats.chroma
        it[ne] = stats.noodle
        it[me] = stats.me
        it[cinema] = stats.cinema

        it[requirements] = requirementsLocal
        it[suggestions] = suggestionsLocal
        it[information] = diff.customData.orNull()?.information?.orNull()?.mapNotNull { it.orNull() }
        it[warnings] = diff.customData.orNull()?.warnings?.orNull()?.mapNotNull { it.orNull() }
    }
}
