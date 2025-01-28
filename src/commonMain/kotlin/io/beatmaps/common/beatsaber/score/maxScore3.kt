package io.beatmaps.common.beatsaber.score

import io.beatmaps.common.beatsaber.map.BSDifficultyV3
import io.beatmaps.common.or

fun generateNoteItems(data: BSDifficultyV3): List<MaxScoreCounterElement> {
    val notes = data.colorNotes.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val sliders = data.sliders.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val burstSliders = data.burstSliders.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()

    val slidersByBeat = sliders.groupBy { it.time }
    val slidersByTailBeat = sliders.groupBy { it.tailTime }
    val burstSlidersByBeat = burstSliders.groupBy { it.time }

    return notes
        .map {
            val matchesHead = slidersByBeat[it.time]?.any { s -> it.color == s.color && it.x == s.x && it.y == s.y } == true
            val matchesTail = slidersByTailBeat[it.time]?.any { s -> it.color == s.color && it.x == s.tailX && it.y == s.tailY } == true
            val matchesBurst = burstSlidersByBeat[it.time]?.any { s -> it.color == s.color && it.x == s.x && it.y == s.y } == true

            val type = if (matchesTail) {
                NoteScoreDefinition.SliderTail
            } else if (matchesBurst) {
                NoteScoreDefinition.BurstSliderHead
            } else if (matchesHead) {
                NoteScoreDefinition.SliderHead
            } else {
                NoteScoreDefinition.Normal
            }

            MaxScoreCounterElement(type, it.time)
        }
}

fun generateBurstItems(data: BSDifficultyV3): List<MaxScoreCounterElement> {
    val burstSliders = data.burstSliders.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()

    return burstSliders.flatMap {
        val sliceCount = it.sliceCount.or(0)
        (1 until sliceCount).map { i ->
            val t = i / (sliceCount - 1).toFloat()
            val beat = (it.time + (it.tailTime - it.time) * t)
            MaxScoreCounterElement(NoteScoreDefinition.BurstSliderElement, beat)
        }
    }
}

fun computeMaxMultipliedScoreForBeatmap(data: BSDifficultyV3) =
    computeMaxMultipliedScoreForBeatmap(
        generateNoteItems(data),
        generateBurstItems(data)
    )
