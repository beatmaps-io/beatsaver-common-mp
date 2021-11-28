package io.beatmaps.common

import io.beatmaps.common.beatsaber.BSDifficulty
import io.beatmaps.common.beatsaber.BSNote
import kotlin.math.pow
import kotlin.math.roundToInt

enum class CutDirection {
    Up, Down, Left, Right,
    UpLeft, UpRight, DownLeft, DownRight,
    Dot;

    companion object {
        private val map = values().associateBy(CutDirection::ordinal)
        fun fromInt(cutDirection: Int) = map[cutDirection] ?: Up
    }
}

// bombs are type 3 for some reason
enum class Types(val _type: Int) {
    Red(0), Blue(1), Bomb(3);

    companion object {
        private val map = values().associateBy(Types::_type)
        fun fromInt(type: Int) = map[type]
    }
}
enum class HitParity {
    Forehand, Backhand
}
enum class Swing {
    Good, Borderline
}
enum class LineIndex {
    Left, MiddleLeft, MiddleRight, Right;

    companion object {
        private val map = values().associateBy(LineIndex::ordinal)
        fun fromInt(lineIndex: Int) = map[lineIndex]
    }
}
enum class LineLayer {
    Bottom, Middle, Top;

    companion object {
        private val map = values().associateBy(LineLayer::ordinal)
        fun fromInt(lineLayer: Int) = map[lineLayer]
    }
}

// the minimum time between the last note or bomb for a bomb to be considered for each saber
// make user configurable?
const val bombMinTime = 0.25

// the tolerance when making float comparisons
// needed because different editors round in different ways
const val comparisonTolerance = 1 / 128f
const val sliderPrecision = 1 / 8f

val cuts = mapOf(
    Types.Blue to mapOf(
        Swing.Good to mapOf(
            HitParity.Forehand to arrayOf(CutDirection.Down, CutDirection.Left, CutDirection.DownLeft, CutDirection.DownRight, CutDirection.Dot),
            HitParity.Backhand to arrayOf(CutDirection.Up, CutDirection.Right, CutDirection.UpLeft, CutDirection.UpRight, CutDirection.DownRight, CutDirection.Dot)
        ),
        Swing.Borderline to mapOf(
            HitParity.Forehand to arrayOf(CutDirection.Right, CutDirection.UpLeft),
            HitParity.Backhand to arrayOf(CutDirection.Left)
        )
    ),
    Types.Red to mapOf(
        Swing.Good to mapOf(
            HitParity.Forehand to arrayOf(CutDirection.Down, CutDirection.Right, CutDirection.DownRight, CutDirection.DownLeft, CutDirection.Dot),
            HitParity.Backhand to arrayOf(CutDirection.Up, CutDirection.Left, CutDirection.UpRight, CutDirection.UpLeft, CutDirection.DownLeft, CutDirection.Dot)
        ),
        Swing.Borderline to mapOf(
            HitParity.Forehand to arrayOf(CutDirection.Left, CutDirection.UpRight),
            HitParity.Backhand to arrayOf(CutDirection.Right)
        )
    )
)

data class ParityPair(val red: Parity = Parity(Types.Red), val blue: Parity = Parity(Types.Blue)) {
    operator fun get(type: Types) = when (type) {
        Types.Red -> red
        Types.Blue -> blue
        else -> throw RuntimeException("Can't get type $type from ParityPair")
    }
    fun init(notes: List<BSNote>) = listOf(red, blue).forEach { it.init(notes) }
}
class Parity(private val type: Types) {
    private var parity = HitParity.Forehand
    var lastInvertTime: Float? = null

    fun getParity() = parity

    fun invert(time: Float) {
        parity = if (parity === HitParity.Forehand) HitParity.Backhand else HitParity.Forehand
        lastInvertTime = time
    }

    fun init(notes: List<BSNote>) {
        val firstNote = notes.firstOrNull { Types.fromInt(it._type) === type }

        parity = if (firstNote?.let { cuts[type]?.get(Swing.Good)?.get(HitParity.Forehand)?.contains(CutDirection.fromInt(it._cutDirection)) } == true) {
            HitParity.Forehand
        } else {
            HitParity.Backhand
        }
    }
}

/**
 * Filters and sorts notes to ensure all notes in array are valid, and assigns an index to each
 * @param {Array} obj - A beat saber JSON array of notes
 * @returns {Array} - filtered, tagged & sorted notes
 */
fun getNotes(obj: BSDifficulty) =
    obj._notes.sortedBy {
        it._time
    }.filter {
        Types.fromInt(it._type) != null
    }

fun Float.toFixed(numOfDec: Int): String {
    val integerDigits = this.toInt()
    val floatDigits = ((this - integerDigits) * 10f.pow(numOfDec)).roundToInt()

    return when {
        floatDigits >= 100 -> "${integerDigits + 1}"
        floatDigits <= 0 -> "$integerDigits"
        floatDigits < 10 -> "$integerDigits.0$floatDigits"
        else -> "$integerDigits.$floatDigits"
    }
}

fun Float.padTime() = this.toInt().padTime()
fun Int.padTime() = this.toString().padStart(2, '0')

fun Float.formatTime() = this.toInt().formatTime()
fun Int.formatTime() =
    if (this > 3600) {
        "${this / 3600}:${((this / 60) % 60).padTime()}"
    } else {
        "${this / 60}"
    }.let { it + ":${(this % 60).padTime()}" }

/**
 * prints a fancy error message to the screen, supports both notes and raw text
 * @param {Array} note - the note responsible for the error (previewed in message). can be omitted
 * @param {String | Number} parity - if in note mode, the type of parity broken, otherwise the time of the error
 * @param {String} message - the caption/description of the error. can be broken into two lines with '|' in text mode
 * @param {String} messageType - the severity of the error - will be added to output as a class
 * @returns {void} - outputs to DOM, should not return a value
 */
enum class MessageType {
    Reset, Warn, Error
}
data class Error(val messageType: MessageType, val time: Float, val parity: HitParity, val type: Types, val column: Int, val row: Int, val deltaTime: Float) {
    companion object {
        fun error(time: Float, parity: HitParity, type: Types, column: Int, row: Int, deltaTime: Float) = Error(MessageType.Error, time, parity, type, column, row, deltaTime)
        fun warn(time: Float, parity: HitParity, type: Types, column: Int, row: Int) = Error(MessageType.Warn, time, parity, type, column, row, 0f)
        fun reset(time: Float, parity: HitParity, type: Types) = Error(MessageType.Reset, time, parity, type, 0, 0, 0f)
    }
}
data class Result(val error: List<Error>, val info: Int, val warnings: Int, val errors: Int)
fun outputReset(resets: MutableList<Error>, note: BSNote, parity: HitParity, type: Types) {
    resets.add(Error.reset(note._time, parity, type))
}
fun outputError(output: MutableList<Error>, note: BSNote, parity: HitParity, type: Types, deltaTime: Float) {
    output.add(Error.error(note._time, parity, type, note._lineIndex, note._lineLayer, deltaTime))
}
fun outputWarn(output: MutableList<Error>, note: BSNote, parity: HitParity, type: Types) {
    output.add(Error.warn(note._time, parity, type, note._lineIndex, note._lineLayer))
}

/**
 * finds the last note in the same colour (for preceding-error highlighting)
 * @param {Array} jsonData - the notes array
 * @param {Number} type - whether the note is a blue or red block
 * @param {Number} lastVal - the position of the error note inside the array
 * @returns {Number} - the index of the last note of the same colour in the array, or -1 if not found
 */
fun findCol(jsonData: List<BSNote>, type: Types, lastVal: Int) =
    if (lastVal < 0) -1 else {
        jsonData.indexOf(
            jsonData.subList(0, lastVal).lastOrNull {
                Types.fromInt(it._type) === type
            }
        )
    }

data class ParityNote(val note: BSNote, var error: Boolean = false, var warn: Boolean = false, var precedingError: Boolean = false, var precedingWarn: Boolean = false)

/**
 * checks for errors in parity within the notes
 * @param map - the map to scan for errors
 * @returns {void} - outputs error messages through outputUI
 */
fun checkParity(map: BSDifficulty): Result {
    val notes = getNotes(map)
    val errors = mutableListOf<Error>()

    val parity = ParityPair().also { it.init(notes) }

    val ohno = map._notes.any { it._lineIndex > 3 || it._lineIndex < 0 || it._lineLayer < 0 || it._lineLayer > 2 }
    if (ohno) {
        // ME is too complicated for me
        return Result(errors, 0, 0, 0)
    }

    val parityNotes = notes.map { ParityNote(it) }

    tailrec fun revertParity(note: BSNote, offset: Int, type: Types) {
        if (offset >= notes.size || (notes[offset]._time - note._time - sliderPrecision) > comparisonTolerance) {
            return
        } else if (note._type == notes[offset]._type) {
            parity[type].invert(note._time)
        } else {
            revertParity(note, offset + 1, type)
        }
    }

    parityNotes.forEachIndexed { i, parityNote ->
        val note = parityNote.note
        val type = Types.fromInt(note._type)
        val cutDirection = CutDirection.fromInt(note._cutDirection)
        val column = LineIndex.fromInt(note._lineIndex)
        val row = LineLayer.fromInt(note._lineLayer)

        if (type == null) {
            return@forEachIndexed
        }

        if (type === Types.Bomb) {
            // this is super ugly, I"m hoping to come up with a better way later
            if (!(arrayOf(LineIndex.MiddleLeft, LineIndex.MiddleRight).contains(column)) || !(arrayOf(LineLayer.Bottom, LineLayer.Top).contains(row))) {
                return@forEachIndexed
            }

            val suggestedParity = if (row == LineLayer.Bottom) HitParity.Forehand else HitParity.Backhand
            val setParity = mutableMapOf(
                Types.Red to true,
                Types.Blue to true
            )
            listOf(Types.Red, Types.Blue).forEach { color ->
                // look ahead for bombMinTime and skip setting parity if it would be set by a note, or inverted back by another bomb
                val targetColumn = if (color === Types.Red) LineIndex.MiddleLeft else LineIndex.MiddleRight
                val targetRow = if (row == LineLayer.Bottom) LineLayer.Top else LineLayer.Bottom
                notes.drop(i + 1).firstOrNull { offsetNote ->
                    val tooSoon = offsetNote._time - note._time - bombMinTime > comparisonTolerance
                    val shouldInvertA = Types.fromInt(offsetNote._type) === color && cuts[color]?.get(Swing.Good)?.get(suggestedParity)?.contains(CutDirection.fromInt(offsetNote._cutDirection)) != true
                    val shouldInvertB = Types.fromInt(offsetNote._type) === Types.Bomb && LineIndex.fromInt(offsetNote._lineIndex) == targetColumn && LineLayer.fromInt(offsetNote._lineLayer) == targetRow

                    if (!tooSoon && (shouldInvertA || shouldInvertB)) {
                        setParity[color] = false
                    }

                    tooSoon || shouldInvertA || shouldInvertB
                }

                parity[color].lastInvertTime?.let { lit ->
                    if (note._time - lit - bombMinTime <= comparisonTolerance) {
                        setParity[color] = false
                    }
                }

                if (suggestedParity == parity[color].getParity()) {
                    parity[color].lastInvertTime = note._time
                }
            }

            // invert parity if needed and log the bomb if so
            setParity.forEach { entry ->
                if (entry.value) {
                    if (suggestedParity !== parity[entry.key].getParity()) {
                        parity[entry.key].invert(note._time)
                        outputReset(errors, note, parity[entry.key].getParity(), entry.key)
                    }
                }
            }
        } else {
            when {
                cuts[type]?.get(Swing.Good)?.get(parity[type].getParity())?.contains(cutDirection) == true -> parity[type].invert(note._time)
                cuts[type]?.get(Swing.Borderline)?.get(parity[type].getParity())?.contains(cutDirection) == true -> {
                    parityNote.warn = true

                    val idx = findCol(notes, type, i - 1)
                    if (idx >= 0) {
                        val last = parityNotes[idx]
                        last.precedingWarn = true
                    }

                    outputWarn(errors, note, parity[type].getParity(), type)
                    parity[type].invert(note._time)
                }
                else -> {
                    parityNote.error = true
                    val deltaTime = note._time - (parity[type].lastInvertTime ?: 0f)
                    val idx = findCol(notes, type, i - 1)
                    if (idx >= 0) {
                        val last = parityNotes[idx]
                        last.precedingError = true
                    }

                    outputError(errors, note, parity[type].getParity(), type, deltaTime)
                }
            }

            // invert parity again if there's a same-color note within sliderPrecision
            revertParity(note, i + 1, type)
        }
    }

    val partitioned = errors.groupBy { it.messageType }
    return Result(errors, partitioned[MessageType.Reset]?.size ?: 0, partitioned[MessageType.Warn]?.size ?: 0, partitioned[MessageType.Error]?.size ?: 0)
}
