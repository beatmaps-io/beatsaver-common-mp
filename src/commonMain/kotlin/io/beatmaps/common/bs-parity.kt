package io.beatmaps.common

import io.beatmaps.common.beatsaber.BSDifficulty
import io.beatmaps.common.beatsaber.BSNote
import kotlin.math.pow
import kotlin.math.roundToInt

val cutDirections = arrayOf("up", "down", "left", "right", "upLeft", "upRight", "downLeft", "downRight", "dot")
fun cutDirection(n: Int) = cutDirections.getOrElse(n) { cutDirections[0] }

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
val lineIndices = arrayOf("left", "middleLeft", "middleRight", "right")
val lineLayers = arrayOf("bottom", "middle", "top")

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
            HitParity.Forehand to arrayOf("down", "left", "downLeft", "downRight", "dot"),
            HitParity.Backhand to arrayOf("up", "right", "upLeft", "upRight", "downRight", "dot")
        ),
        Swing.Borderline to mapOf(
            HitParity.Forehand to arrayOf("right", "upLeft"),
            HitParity.Backhand to arrayOf("left")
        )
    ),
    Types.Red to mapOf(
        Swing.Good to mapOf(
            HitParity.Forehand to arrayOf("down", "right", "downRight", "downLeft", "dot"),
            HitParity.Backhand to arrayOf("up", "left", "upRight", "upLeft", "downLeft", "dot")
        ),
        Swing.Borderline to mapOf(
            HitParity.Forehand to arrayOf("left", "upRight"),
            HitParity.Backhand to arrayOf("right")
        )
    )
)

class Parity {
    private val colors = mutableMapOf<Types, HitParity>()

    init {
        colors[Types.Red] = HitParity.Forehand
        colors[Types.Blue] = HitParity.Forehand
    }

    operator fun get(type: Types) = colors[type]!!
    operator fun set(type: Types, v: HitParity) {
        colors[type] = v
    }

    fun invert(color: Types) {
        if (this[color] === HitParity.Forehand) this[color] = HitParity.Backhand else this[color] = HitParity.Forehand
    }

    fun init(notes: List<BSNote>) {
        val firstRed: BSNote? = notes.firstOrNull { Types.fromInt(it._type) === Types.Red }
        val firstBlue: BSNote? = notes.firstOrNull { Types.fromInt(it._type) === Types.Blue }

        if (firstRed?.let { cuts[Types.Red]?.get(Swing.Good)?.get(HitParity.Forehand)?.contains(cutDirection(it._cutDirection)) } == true) {
            this[Types.Red] = HitParity.Forehand
        } else {
            this[Types.Blue] = HitParity.Backhand
        }

        if (firstBlue?.let { cuts[Types.Blue]?.get(Swing.Good)?.get(HitParity.Forehand)?.contains(cutDirection(it._cutDirection)) } == true) {
            this[Types.Blue] = HitParity.Forehand
        } else {
            this[Types.Blue] = HitParity.Backhand
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
        else -> "$integerDigits.$floatDigits"
    }
}

fun Float.padTime() = this.toInt().toString().padStart(2, '0')

fun Float.formatTime() =
    if (this > 3600) {
        "${(this / 3600).toInt()}:${((this / 60) % 60).padTime()}"
    } else {
        "${(this / 60).toInt()}"
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

    val parity = Parity()
    parity.init(notes)

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
            parity.invert(type)
        } else {
            revertParity(note, offset + 1, type)
        }
    }

    tailrec fun bombReset(offset: Int, note: BSNote, setParity: MutableMap<Types, Boolean>) {
        if (offset < 0 || (note._time - notes[offset]._time - bombMinTime) > comparisonTolerance) {
            return
        } else if (Types.fromInt(notes[offset]._type) == Types.Bomb) {
            if (lineIndices[notes[offset]._lineIndex] === "middleLeft") {
                setParity[Types.Red] = false
            } else if (lineIndices[notes[offset]._lineIndex] === "middleRight") {
                setParity[Types.Blue] = false
            }
        } else if (Types.fromInt(notes[offset]._type) == Types.Red) {
            setParity[Types.Red] = false
        } else if (Types.fromInt(notes[offset]._type) == Types.Blue) {
            setParity[Types.Blue] = false
        }
        bombReset(offset - 1, note, setParity)
    }

    parityNotes.forEachIndexed { i, parityNote ->
        val note = parityNote.note
        val type = Types.fromInt(note._type)
        val cutDirection = cutDirection(note._cutDirection)
        val column = lineIndices[note._lineIndex]
        val row = lineLayers[note._lineLayer]

        if (type == null) {
            return@forEachIndexed
        }

        if (type === Types.Bomb) {
            // this is super ugly, I"m hoping to come up with a better way later
            if (!(arrayOf("middleLeft", "middleRight").contains(column)) || !(arrayOf("bottom", "top").contains(row))) {
                return@forEachIndexed
            }

            // for each saber: ignore the bomb if it"s within bombMinTime after a note or own-side bomb that says otherwise
            val setParity = mutableMapOf(
                Types.Red to true,
                Types.Blue to true
            )
            bombReset(i - 1, note, setParity)

            // invert parity if needed and log the bomb if so
            setParity.forEach { entry ->
                if (entry.value) {
                    if ((row === "bottom" && parity[entry.key] === HitParity.Backhand) || (row === "top" && parity[entry.key] === HitParity.Forehand)) {
                        parity.invert(entry.key)
                        outputReset(errors, note, parity[entry.key], entry.key)
                    }
                }
            }
        } else {
            when {
                cuts[type]?.get(Swing.Good)?.get(parity[type])?.contains(cutDirection) == true -> parity.invert(type)
                cuts[type]?.get(Swing.Borderline)?.get(parity[type])?.contains(cutDirection) == true -> {
                    parityNote.warn = true

                    val idx = findCol(notes, type, i - 1)
                    if (idx >= 0) {
                        val last = parityNotes[idx]
                        last.precedingWarn = true
                    }

                    outputWarn(errors, note, parity[type], type)
                    parity.invert(type)
                }
                else -> {
                    parityNote.error = true
                    val idx = findCol(notes, type, i - 1)
                    val deltaTime: Float
                    if (idx >= 0) {
                        val last = parityNotes[idx]
                        deltaTime = note._time - last.note._time
                        last.precedingError = true
                    } else {
                        deltaTime = 0f
                    }

                    outputError(errors, note, parity[type], type, deltaTime)
                }
            }

            // invert parity again if there's a same-color note within sliderPrecision
            revertParity(note, i + 1, type)
        }
    }

    val partitioned = errors.groupBy { it.messageType }
    return Result(errors, partitioned[MessageType.Reset]?.size ?: 0, partitioned[MessageType.Warn]?.size ?: 0, partitioned[MessageType.Error]?.size ?: 0)
}
