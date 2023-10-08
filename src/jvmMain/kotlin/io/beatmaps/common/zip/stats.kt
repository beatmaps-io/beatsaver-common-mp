package io.beatmaps.common.zip

import io.beatmaps.common.beatsaber.BSDiff
import io.beatmaps.common.beatsaber.DifficultyBeatmap
import io.beatmaps.common.beatsaber.DifficultyBeatmapSet
import io.beatmaps.common.beatsaber.MapInfo
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.checkParity
import io.beatmaps.common.dbo.Difficulty
import io.beatmaps.common.dbo.Versions
import io.beatmaps.common.dbo.VersionsDao
import io.beatmaps.common.dbo.maxAllowedNps
import io.beatmaps.common.pow
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.lang.Float.min
import java.math.BigDecimal

data class DiffStats(val chroma: Boolean, val noodle: Boolean, val me: Boolean, val cinema: Boolean, val nps: BigDecimal)
fun Array<String>?.containsIgnoreCase(element: String) = this?.any { e -> e.equals(element, true) } ?: false

fun ZipHelper.parseDifficulty(hash: String, diff: DifficultyBeatmap, char: DifficultyBeatmapSet, map: MapInfo, sli: SongLengthInfo, ver: VersionsDao? = null): DiffStats {
    val version = ver ?: VersionsDao.wrapRow(
        Versions.select {
            Versions.hash eq hash
        }.first()
    )

    var stats = DiffStats(chroma = false, noodle = false, me = false, cinema = false, nps = BigDecimal.ZERO)

    Difficulty.insertIgnore {
        it[mapId] = version.mapId
        it[versionId] = version.id
        it[createdAt] = version.uploaded

        val bsdiff = diff(diff._beatmapFilename)

        stats = sharedInsert(it, diff, bsdiff, map, sli)
        it[characteristic] = char.enumValue()
        it[difficulty] = diff.enumValue()
    }

    return stats
}

fun Difficulty.sharedInsert(it: UpdateBuilder<*>, diff: DifficultyBeatmap, bsdiff: BSDiff, map: MapInfo, sli: SongLengthInfo): DiffStats {
    it[njs] = diff._noteJumpMovementSpeed
    it[offset] = diff._noteJumpStartBeatOffset

    checkParity(bsdiff).also { pr ->
        it[pReset] = pr.info
        it[pError] = pr.errors
        it[pWarn] = pr.warnings
    }

    val maxLen = 10.pow(7) - 1.0f

    val len = bsdiff.songLength()
    val noteCount = bsdiff.noteCount()
    val mappedNps = bsdiff.mappedNps(sli)

    it[schemaVersion] = bsdiff.version.orNull() ?: "2.2.0"
    it[notes] = noteCount
    it[bombs] = bsdiff.bombCount()
    it[arcs] = bsdiff.arcCount()
    it[chains] = bsdiff.chainCount()
    it[obstacles] = bsdiff.obstacleCount()
    it[events] = bsdiff.eventCount()
    it[length] = min(len, maxLen).toBigDecimal()
    it[seconds] = min(if (map._beatsPerMinute == 0f) 0f else (60 / map._beatsPerMinute) * len, maxLen).toBigDecimal()
    it[maxScore] = bsdiff.maxScore()
    it[label] = diff._customData?._difficultyLabel?.take(255)

    val requirementsLocal = diff._customData?._requirements?.toTypedArray()
    val suggestionsLocal = diff._customData?._suggestions?.toTypedArray()

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
        it[information] = diff._customData?._information?.toTypedArray()
        it[warnings] = diff._customData?._warnings?.toTypedArray()
    }
}
