package io.beatmaps.common.beatsaber

data class ScoreMultiplierCounter(
    val multiplier: Int = 1,
    val multiplierIncreaseProgress: Int = 0,
    val multiplierIncreaseMaxProgress: Int = 2
) {

    fun normalizedProgress() = multiplierIncreaseProgress / multiplierIncreaseMaxProgress.toFloat()
    fun processMultiplierEvent(type: MultiplierEventType) =
        when (type) {
            MultiplierEventType.Positive -> {
                if (multiplier < 8) {
                    if (multiplierIncreaseProgress >= multiplierIncreaseMaxProgress - 1) {
                        ScoreMultiplierCounter(multiplier * 2, 0, multiplier * 4)
                    } else {
                        copy(multiplierIncreaseProgress = multiplierIncreaseProgress + 1)
                    }
                } else {
                    copy()
                }
            }
            MultiplierEventType.Negative -> {
                if (multiplierIncreaseProgress > 0) {
                    copy(multiplierIncreaseProgress = 0)
                } else if (multiplier > 1) {
                    copy(multiplier = multiplier / 2, multiplierIncreaseMaxProgress = multiplier)
                } else {
                    copy()
                }
            }
            MultiplierEventType.Neutral -> copy()
        }
}

enum class MultiplierEventType {
    Positive, Neutral, Negative
}

data class MaxScoreCounterElement(val scoreDef: NoteScoreDefinition, val time: Float)
enum class NoteScoreDefinition(
    val maxCenterDistanceCutScore: Int,
    val minBeforeCutScore: Int,
    val maxBeforeCutScore: Int,
    val minAfterCutScore: Int,
    val maxAfterCutScore: Int,
    val fixedCutScore: Int
) {
    NoScore(0, 0, 0, 0, 0, 0),
    Normal(NoteScoreDefinition.maxCenterDistanceCutScore, 0, NoteScoreDefinition.maxBeforeCutScore, 0, NoteScoreDefinition.maxAfterCutScore, 0),
    SliderHead(NoteScoreDefinition.maxCenterDistanceCutScore, 0, NoteScoreDefinition.maxBeforeCutScore, NoteScoreDefinition.maxAfterCutScore, NoteScoreDefinition.maxAfterCutScore, 0),
    SliderTail(NoteScoreDefinition.maxCenterDistanceCutScore, NoteScoreDefinition.maxBeforeCutScore, NoteScoreDefinition.maxBeforeCutScore, 0, NoteScoreDefinition.maxAfterCutScore, 0),
    BurstSliderHead(NoteScoreDefinition.maxCenterDistanceCutScore, 0, NoteScoreDefinition.maxBeforeCutScore, 0, 0, 0),
    BurstSliderElement(0, 0, 0, 0, 0, 20);

    fun maxCutScore() = maxCenterDistanceCutScore + maxBeforeCutScore + maxAfterCutScore + fixedCutScore

    companion object {
        const val maxBeforeCutScore = 70
        const val maxCenterDistanceCutScore = 15
        const val maxAfterCutScore = 30
    }
}

fun computeMaxMultipliedScoreForBeatmap(data: BSDifficultyV3): Int {
    val notes = data.colorNotes
    val sliders = data.sliders
    val burstSliders = data.burstSliders

    val slidersByBeat = sliders.groupBy { it.beat }
    val slidersByTailBeat = sliders.groupBy { it.tailBeat }
    val burstSlidersByBeat = burstSliders.groupBy { it.beat }

    val noteItems = notes
        .map {
            val matchesHead = slidersByBeat[it.beat]?.any { s -> it.color == s.color && it.x == s.x && it.y == s.y } == true
            val matchesTail = slidersByTailBeat[it.beat]?.any { s -> it.color == s.color && it.x == s.tailX && it.y == s.tailY } == true
            val matchesBurst = burstSlidersByBeat[it.beat]?.any { s -> it.color == s.color && it.x == s.x && it.y == it.y } == true

            val type = if (matchesTail) {
                NoteScoreDefinition.SliderTail
            } else if (matchesBurst) {
                NoteScoreDefinition.BurstSliderHead
            } else if (matchesHead) {
                NoteScoreDefinition.SliderHead
            } else {
                NoteScoreDefinition.Normal
            }

            MaxScoreCounterElement(type, it.beat)
        }

    val burstItems = burstSliders.flatMap {
        (1 until it.sliceCount).map { i ->
            val t = i / (it.sliceCount - 1).toFloat()
            val beat = (it.beat + (it.tailBeat - it.beat) * t)
            MaxScoreCounterElement(NoteScoreDefinition.BurstSliderElement, beat)
        }
    }

    val items = (noteItems + burstItems).sortedWith(compareBy<MaxScoreCounterElement> { it.time }.thenBy { it.scoreDef.maxCutScore() })

    return items.fold(ScoreMultiplierCounter() to 0) { (smc, score), elem ->
        smc.processMultiplierEvent(MultiplierEventType.Positive).let { newSmc ->
            newSmc to (score + (elem.scoreDef.maxCutScore() * newSmc.multiplier))
        }
    }.second
}
