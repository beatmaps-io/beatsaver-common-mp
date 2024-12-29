package io.beatmaps.common.dbo

import io.beatmaps.common.api.AiDeclarationType
import io.beatmaps.common.api.EBeatsaberEnvironment
import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.api.EMapState
import io.beatmaps.common.db.postgresEnumeration
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryAlias
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.wrapAsExpression
import java.math.BigDecimal
import java.time.Instant

object Beatmap : IntIdTable("beatmap", "mapId") {
    val name = text("name")
    val description = text("description")
    val uploader = reference("uploader", User)
    val bpm = float("bpm")
    val duration = integer("duration")
    val plays = integer("plays")
    val downloads = integer("downloads")

    val createdAt = timestamp("createdAt")
    val uploaded = timestamp("uploaded").nullable()
    val updatedAt = timestamp("updatedAt")
    val lastPublishedAt = timestamp("lastPublishedAt").nullable()
    val deletedAt = timestamp("deletedAt").nullable()

    val curator = optReference("curatedBy", User)
    val curatedAt = timestamp("curatedAt").nullable()

    val beatsaverDownloads = integer("bsdownload")
    val upVotes = integer("bsupvote")
    val downVotes = integer("bsdownvote")
    val score = decimal("score", 4, 4)
    val upVotesInt = integer("upvote")
    val downVotesInt = integer("downvote")
    val lastVoteAt = timestamp("lastVoteAt").nullable()
    val sentiment = decimal("sentiment", 4, 3)
    val reviews = integer("reviews")

    val ranked = bool("ranked")
    val qualified = bool("qualified")
    val blRanked = bool("blRanked")
    val blQualified = bool("blQualified")
    val rankedAt = timestamp("rankedAt").nullable()
    val qualifiedAt = timestamp("qualifiedAt").nullable()
    val blRankedAt = timestamp("blRankedAt").nullable()
    val blQualifiedAt = timestamp("blQualifiedAt").nullable()

    val minNps = decimal("minNps", 8, 3)
    val maxNps = decimal("maxNps", 8, 3)

    val tags = array("tags", VarCharColumnType(255)).nullable()

    val declaredAi = postgresEnumeration<AiDeclarationType>("declaredAi", "aiDeclarationType")
}

data class BeatmapDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<BeatmapDao>(Beatmap)
    val name: String by Beatmap.name
    val description: String by Beatmap.description
    val plays: Int by Beatmap.plays
    val downloads: Int by Beatmap.downloads

    val createdAt by Beatmap.createdAt
    val uploaded by Beatmap.uploaded
    val updatedAt by Beatmap.updatedAt
    val lastPublishedAt by Beatmap.lastPublishedAt
    val deletedAt by Beatmap.deletedAt

    val curatedAt by Beatmap.curatedAt
    val curator by UserDao optionalReferencedOn Beatmap.curator

    val uploaderId by Beatmap.uploader
    val uploader by UserDao referencedOn Beatmap.uploader
    val versions = mutableMapOf<EntityID<Int>, VersionsDao>()

    val score by Beatmap.score
    val upVotesInt by Beatmap.upVotesInt
    val downVotesInt by Beatmap.downVotesInt

    val ranked by Beatmap.ranked
    val qualified by Beatmap.qualified
    val blRanked by Beatmap.blRanked
    val blQualified by Beatmap.blQualified

    val minNps by Beatmap.minNps
    val maxNps by Beatmap.maxNps

    val tags by Beatmap.tags

    val sentiment by Beatmap.sentiment
    val reviews by Beatmap.reviews

    val declaredAi by Beatmap.declaredAi

    var bookmarked: Boolean? = null

    val collaborators = mutableMapOf<EntityID<Int>, UserDao>()

    fun enrichTestplays() = this.also {
        val v = versions.filter { it.value.state != EMapState.Published }
        if (v.isNotEmpty()) {
            val testplayResults = Testplay
                .joinUser(Testplay.userId)
                .selectAll()
                .where {
                    Testplay.versionId inList v.map { it.key.value }.toList()
                }.toList()

            val feedback = testplayResults.map { row ->
                UserDao.wrapRow(row) // Cache user info from query
                TestplayDao.wrapRow(row)
            }.groupBy { it.versionId }

            v.forEach {
                feedback[it.key]?.associateBy { inner -> inner.id }?.let { m ->
                    it.value.testplays.putAll(m)
                }
            }
        }
    }
}

fun ColumnSet.joinVersions(stats: Boolean = false, column: Expression<*>? = Beatmap.id, state: (SqlExpressionBuilder.() -> Op<Boolean>)? = { Versions.state eq EMapState.Published }) =
    join(Versions, JoinType.INNER, onColumn = column, otherColumn = Versions.mapId, additionalConstraint = state).run {
        if (stats) {
            join(Difficulty, JoinType.INNER, onColumn = Versions.id, otherColumn = Difficulty.versionId)
        } else {
            this
        }
    }
fun ColumnSet.joinUploader() = join(User, JoinType.INNER, onColumn = Beatmap.uploader, otherColumn = User.id)
fun ColumnSet.joinCurator() = join(curatorAlias, JoinType.LEFT, onColumn = Beatmap.curator, otherColumn = curatorAlias[User.id])
fun ColumnSet.joinBookmarked(userId: Int?) =
    join(bookmark, JoinType.LEFT, onColumn = Beatmap.id, otherColumn = bookmark[PlaylistMap.mapId]) {
        bookmark[PlaylistMap.playlistId] eq wrapAsExpression(
            User
                .select(User.bookmarksId)
                .where { User.id eq userId }
                .limit(1)
        )
    }

fun ColumnSet.joinCollaborations() = join(Collaboration, JoinType.LEFT, Beatmap.id, Collaboration.mapId) {
    Collaboration.accepted eq true
}

fun ColumnSet.joinCollaborators() = joinCollaborations()
    .join(collaboratorAlias, JoinType.LEFT, Collaboration.collaboratorId, collaboratorAlias[User.id])

fun Query.complexToBeatmap(alias: QueryAlias? = null, cb: (ResultRow) -> Unit = {}) = this.fold(mutableMapOf<EntityID<Int>, BeatmapDao>()) { map, row ->
    map.also {
        map.getOrPut(row[alias?.get(Beatmap.id) ?: Beatmap.id]) {
            if (row.hasValue(User.id)) {
                UserDao.wrapRow(row)
            }
            if (row.hasValue(curatorAlias[User.id]) && row[Beatmap.curator] != null) {
                UserDao.wrapRow(row, curatorAlias)
            }

            cb(row)

            if (alias != null) {
                BeatmapDao.wrapRow(row, alias)
            } else {
                BeatmapDao.wrapRow(row)
            }
        }.run {
            if (row.hasValue(Versions.id)) {
                versions.getOrPut(row[Versions.id]) {
                    VersionsDao.wrapRow(row)
                }.apply {
                    if (row.hasValue(Difficulty.id)) {
                        difficulties.getOrPut(row[Difficulty.id]) {
                            DifficultyDao.wrapRow(row)
                        }
                    }
                }.run {
                    if (row.hasValue(Testplay.id)) {
                        testplays.getOrPut(row[Testplay.id]) {
                            TestplayDao.wrapRow(row)
                        }
                    }
                }
            }

            bookmarked = if (row.hasValue(bookmark[PlaylistMap.id])) row.getOrNull(bookmark[PlaylistMap.id]) != null else null

            row.getOrNull(collaboratorAlias[User.id])?.let {
                collaborators.getOrPut(it) {
                    UserDao.wrapRow(row, collaboratorAlias)
                }
            }
        }
    }
}.values.toList()

object Versions : IntIdTable("versions", "versionId") {
    val mapId = reference("mapId", Beatmap)
    val hash = char("hash", 40)
    val uploaded = timestamp("createdAt")
    val state = postgresEnumeration<EMapState>("state", "mapstate")
    val feedback = text("feedback").nullable()
    val testplayAt = timestamp("testplayAt").nullable()
    val key64 = varchar("key64", 8).nullable()
    val sageScore = short("sageScore").nullable()
    val scheduledAt = timestamp("scheduledAt").nullable()
    val r2 = bool("r2")
    val deletedAt = timestamp("deletedAt").nullable()
    val lastPublishedAt = timestamp("lastPublishedAt").nullable()
    val schemaVersion = varchar("schemaVersion", 10).nullable()

    val bpm = float("bpm")
    val duration = integer("duration")
    val songName = text("songName")
    val songSubName = text("songSubName")
    val songAuthorName = text("songAuthorName")
    val levelAuthorName = text("levelAuthorName")
}

data class VersionsDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<VersionsDao>(Versions)
    val mapId: EntityID<Int> by Versions.mapId
    val map: BeatmapDao by BeatmapDao referencedOn Versions.mapId
    val hash: String by Versions.hash
    val uploaded: Instant by Versions.uploaded
    val state: EMapState by Versions.state
    val feedback: String? by Versions.feedback
    val testplayAt: Instant? by Versions.testplayAt
    val key64: String? by Versions.key64
    val sageScore by Versions.sageScore
    val scheduledAt by Versions.scheduledAt
    val r2 by Versions.r2
    val deletedAt by Versions.deletedAt
    val lastPublishedAt by Versions.lastPublishedAt

    val bpm: Float by Versions.bpm
    val duration: Int by Versions.duration
    val songName: String by Versions.songName
    val songSubName: String by Versions.songSubName
    val songAuthorName: String by Versions.songAuthorName
    val levelAuthorName: String by Versions.levelAuthorName

    val testplays = mutableMapOf<EntityID<Int>, TestplayDao>()
    val difficulties = mutableMapOf<EntityID<Int>, DifficultyDao>()
}

val maxAllowedNps = BigDecimal.valueOf(99999L)
object Difficulty : IntIdTable("difficulty", "difficultyId") {
    val versionId = reference("versionId", Versions)
    val njs = float("njs")
    val offset = float("offsetTime")
    val notes = integer("notes")
    val bombs = integer("bombs")
    val arcs = integer("arcs")
    val chains = integer("chains")
    val obstacles = integer("obstacles")
    val nps = decimal("nps", 8, 3)
    val length = decimal("length", 10, 3)
    val seconds = decimal("seconds", 10, 3)
    val mapId = reference("mapId", Beatmap)
    val characteristic = postgresEnumeration<ECharacteristic>("characteristic", "characteristic")
    val difficulty = postgresEnumeration<EDifficulty>("difficulty", "diff")
    val events = integer("events")
    val pReset = integer("pReset")
    val pWarn = integer("pWarn")
    val pError = integer("pError")
    val createdAt = timestamp("createdAt")
    val stars = decimal("stars", 4, 2).nullable()
    val blStars = decimal("blStars", 4, 2).nullable()
    val requirements = array("requirements", VarCharColumnType(64)).nullable()
    val suggestions = array("suggestions", VarCharColumnType(255)).nullable()
    val information = array("information", VarCharColumnType(255)).nullable()
    val warnings = array("warnings", VarCharColumnType(255)).nullable()
    val label = varchar("label", 255).nullable()
    val environment = postgresEnumeration<EBeatsaberEnvironment>("environment", "environment").nullable()

    val maxScore = integer("maxScore")
    val schemaVersion = varchar("schemaVersion", 10)

    val rankedAt = timestamp("rankedAt").nullable()
    val qualifiedAt = timestamp("qualifiedAt").nullable()
    val blRankedAt = timestamp("blRankedAt").nullable()
    val blQualifiedAt = timestamp("blQualifiedAt").nullable()

    val uniqueDiff = Index(listOf(versionId, characteristic, difficulty), true, "diff_unique")
}

data class DifficultyDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<DifficultyDao>(Difficulty)
    val version: VersionsDao by VersionsDao referencedOn Difficulty.versionId
    val njs: Float by Difficulty.njs
    val offset: Float by Difficulty.offset
    val notes: Int by Difficulty.notes
    val bombs: Int by Difficulty.bombs
    val obstacles: Int by Difficulty.obstacles
    val nps: BigDecimal by Difficulty.nps
    val length: BigDecimal by Difficulty.length
    val seconds: BigDecimal by Difficulty.seconds
    val map: BeatmapDao by BeatmapDao referencedOn Difficulty.mapId
    val characteristic: ECharacteristic by Difficulty.characteristic
    val difficulty: EDifficulty by Difficulty.difficulty
    val events: Int by Difficulty.events
    val pReset: Int by Difficulty.pReset
    val pWarn: Int by Difficulty.pWarn
    val pError: Int by Difficulty.pError
    val createdAt: Instant by Difficulty.createdAt
    var stars: BigDecimal? by Difficulty.stars
    var blStars: BigDecimal? by Difficulty.blStars
    val requirements by Difficulty.requirements
    val suggestions by Difficulty.suggestions
    val information by Difficulty.information
    val warnings by Difficulty.warnings
    val maxScore by Difficulty.maxScore
    val rankedAt by Difficulty.rankedAt
    val qualifiedAt by Difficulty.qualifiedAt
    val blRankedAt by Difficulty.blRankedAt
    val blQualifiedAt by Difficulty.blQualifiedAt
    val label by Difficulty.label
    val environment by Difficulty.environment
}
