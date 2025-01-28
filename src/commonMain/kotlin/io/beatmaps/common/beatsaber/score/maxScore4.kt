package io.beatmaps.common.beatsaber.score

import io.beatmaps.common.beatsaber.map.BSDifficultyV4

fun generateNoteItems(data: BSDifficultyV4): List<MaxScoreCounterElement> {
    val notes = data.colorNotes.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val sliders = data.arcs.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val burstSliders = data.chains.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()

    val slidersByBeat = sliders.groupBy { it.time }
    val slidersByTailBeat = sliders.groupBy { it.tailTime }
    val burstSlidersByBeat = burstSliders.groupBy { it.time }

    return notes
        .map {
            val noteData = it.getData(data)
            val matchesHead = slidersByBeat[it.time]?.mapNotNull { s -> s.getHead(data) }?.any { head ->
                noteData?.color == head.color && noteData.x == head.x && noteData.y == head.y
            } == true
            val matchesTail = slidersByTailBeat[it.time]?.mapNotNull { s -> s.getTail(data) }?.any { tail ->
                noteData?.color == tail.color && noteData.x == tail.x && noteData.y == tail.y
            } == true
            val matchesBurst = burstSlidersByBeat[it.time]?.mapNotNull { s -> s.getHead(data) }?.any { head ->
                noteData?.color == head.color && noteData.x == head.x && noteData.y == head.y
            } == true

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

fun generateBurstItems(data: BSDifficultyV4): List<MaxScoreCounterElement> {
    val burstSliders = data.chains.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()

    return burstSliders.flatMap {
        val chain = it.getData(data)
        val sliceCount = chain?.sliceCount?.orNull() ?: 0
        (1 until sliceCount).map { i ->
            val t = i / (sliceCount - 1).toFloat()
            val beat = (it.time + (it.tailTime - it.time) * t)
            MaxScoreCounterElement(NoteScoreDefinition.BurstSliderElement, beat)
        }
    }
}

fun computeMaxMultipliedScoreForBeatmap(data: BSDifficultyV4) =
    computeMaxMultipliedScoreForBeatmap(
        generateNoteItems(data),
        generateBurstItems(data)
    )
