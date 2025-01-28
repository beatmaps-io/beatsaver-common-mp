@file:UseSerializers(OptionalPropertySerializer::class)

package io.beatmaps.common.beatsaber.map

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.OptionalPropertySerializer
import io.beatmaps.common.beatsaber.SongLengthInfo
import io.beatmaps.common.beatsaber.custom.BSMapCustomData
import io.beatmaps.common.beatsaber.score.computeMaxMultipliedScoreForBeatmap
import io.beatmaps.common.or
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.reflect.KProperty1

@Serializable
data class BSDifficultyV4(
    override val version: OptionalProperty<String?> = OptionalProperty.NotPresent,
    val colorNotes: OptionalProperty<List<OptionalProperty<BSNoteV4?>>?> = OptionalProperty.NotPresent,
    val colorNotesData: OptionalProperty<List<OptionalProperty<BSNoteDataV4?>>?> = OptionalProperty.NotPresent,
    val bombNotes: OptionalProperty<List<OptionalProperty<BSBombV4?>>?> = OptionalProperty.NotPresent,
    val bombNotesData: OptionalProperty<List<OptionalProperty<BSBombDataV4?>>?> = OptionalProperty.NotPresent,
    val obstacles: OptionalProperty<List<OptionalProperty<BSObstacleV4?>>?> = OptionalProperty.NotPresent,
    val obstaclesData: OptionalProperty<List<OptionalProperty<BSObstacleDataV4?>>?> = OptionalProperty.NotPresent,
    val arcs: OptionalProperty<List<OptionalProperty<BSArcV4?>>?> = OptionalProperty.NotPresent,
    val arcsData: OptionalProperty<List<OptionalProperty<BSArcDataV4?>>?> = OptionalProperty.NotPresent,
    val chains: OptionalProperty<List<OptionalProperty<BSChainV4?>>?> = OptionalProperty.NotPresent,
    val chainsData: OptionalProperty<List<OptionalProperty<BSChainDataV4?>>?> = OptionalProperty.NotPresent,
    val spawnRotations: OptionalProperty<List<OptionalProperty<BSRotationsV4?>>?> = OptionalProperty.NotPresent,
    val spawnRotationsData: OptionalProperty<List<OptionalProperty<BSRotationsDataV4?>>?> = OptionalProperty.NotPresent,
    val njsEvents: OptionalProperty<List<OptionalProperty<BSNjsEventV4?>>?> = OptionalProperty.NotPresent,
    val njsEventData: OptionalProperty<List<OptionalProperty<BSNjsEventDataV4?>>?> = OptionalProperty.NotPresent,
    override val customData: OptionalProperty<BSDifficultyV4CustomData?> = OptionalProperty.NotPresent
) : BSDiff {
    override fun noteCount() = colorNotes.orEmpty().size
    override fun bombCount() = bombNotes.orEmpty().size
    override fun arcCount() = arcs.orEmpty().size
    override fun chainCount() = chains.orEmpty().size

    override fun obstacleCount() = obstacles.orEmpty().size
    private val firstAndLastLazy by lazy {
        colorNotes.orEmpty().sortedBy { note -> note.time }.let { sorted ->
            if (sorted.isNotEmpty()) {
                sorted.first().time to sorted.last().time
            } else {
                0f to 0f
            }
        }
    }
    override fun songLength() = firstAndLastLazy.second - firstAndLastLazy.first

    private val maxScoreLazy by lazy { computeMaxMultipliedScoreForBeatmap(this) }
    override fun maxScore() = maxScoreLazy
    override fun mappedNps(sli: SongLengthInfo) =
        sli.let {
            it.timeToSeconds(firstAndLastLazy.second) - it.timeToSeconds(firstAndLastLazy.first)
        }.let { len ->
            if (len == 0f) 0f else noteCount() / len
        }
}

@Serializable
data class BSDifficultyV4CustomData(
    override val time: OptionalProperty<Float?>
) : BSMapCustomData

typealias BSIndexedV4<T> = BSIndexedGeneric<BSDifficultyV4, T>

abstract class BSIndexedGeneric<U, T : BSIndexable> : BSIndexed {
    abstract val prop: KProperty1<U, OptionalProperty<List<OptionalProperty<T?>>?>>

    fun getData(diff: U) =
        getForProp(index, prop.get(diff))

    protected fun <T : BSIndexable> getForProp(index: OptionalProperty<Int?>, list: OptionalProperty<List<OptionalProperty<T?>>?>) =
        list.orEmpty().getOrNull(index.or(0))
}

interface BSIndexed {
    val index: OptionalProperty<Int?>
}

interface BSIndexable

@Serializable
data class BSNoteV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSNoteDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::colorNotesData
}

@Serializable
data class BSNoteDataV4(
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val color: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("d")
    val direction: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val angleOffset: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSBombV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSBombDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::bombNotesData
}

@Serializable
data class BSBombDataV4(
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSObstacleV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSObstacleDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::obstaclesData
}

@Serializable
data class BSObstacleDataV4(
    @SerialName("d")
    val duration: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("w")
    val width: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("h")
    val height: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSArcV4(
    @SerialName("hb")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tb")
    val tailBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("hr")
    val headRotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("tr")
    val tailRotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("hi")
    val headIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ti")
    val tailIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ai")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSArcDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::arcsData

    val tailTime by orNegativeInfinity { tailBeat }
    fun getHead(diff: BSDifficultyV4) = getForProp(headIndex, diff.colorNotesData)
    fun getTail(diff: BSDifficultyV4) = getForProp(tailIndex, diff.colorNotesData)
}

@Serializable
data class BSArcDataV4(
    @SerialName("m")
    val headControlPointLengthMultiplier: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tm")
    val tailControlPointLengthMultiplier: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("a")
    val midAnchorMode: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSChainV4(
    @SerialName("hb")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("tb")
    val tailBeat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("hr")
    val headRotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("tr")
    val tailRotationLane: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("i")
    val headIndex: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ci")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSChainDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::chainsData

    val tailTime by orNegativeInfinity { tailBeat }
    fun getHead(diff: BSDifficultyV4) = getForProp(headIndex, diff.colorNotesData)
}

@Serializable
data class BSChainDataV4(
    @SerialName("tx")
    val x: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("ty")
    val y: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("c")
    val sliceCount: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("s")
    val squishAmount: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSRotationsV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSRotationsDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::spawnRotationsData
}

@Serializable
data class BSRotationsDataV4(
    @SerialName("t")
    val executionTime: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("r")
    val rotation: OptionalProperty<Float?> = OptionalProperty.NotPresent
) : BSIndexable

@Serializable
data class BSNjsEventV4(
    @SerialName("b")
    override val beat: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("i")
    override val index: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexedV4<BSNjsEventDataV4>(), IBSObject by BSObject(beat) {
    override val prop = BSDifficultyV4::njsEventData
}

@Serializable
data class BSNjsEventDataV4(
    @SerialName("d")
    val relativeNoteJumpSpeed: OptionalProperty<Float?> = OptionalProperty.NotPresent,
    @SerialName("p")
    val usePreviousValue: OptionalProperty<Int?> = OptionalProperty.NotPresent,
    @SerialName("e")
    val type: OptionalProperty<Int?> = OptionalProperty.NotPresent
) : BSIndexable
