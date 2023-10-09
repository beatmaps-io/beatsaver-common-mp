package io.beatmaps.common.beatsaber

import io.beatmaps.common.or

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
object NoteScoreDefaults {
    const val maxBeforeCutScore = 70
    const val maxCenterDistanceCutScore = 15
    const val maxAfterCutScore = 30
}
enum class NoteScoreDefinition(
    val maxCenterDistanceCutScore: Int,
    val minBeforeCutScore: Int,
    val maxBeforeCutScore: Int,
    val minAfterCutScore: Int,
    val maxAfterCutScore: Int,
    val fixedCutScore: Int
) {
    NoScore(0, 0, 0, 0, 0, 0),
    Normal(NoteScoreDefaults.maxCenterDistanceCutScore, 0, NoteScoreDefaults.maxBeforeCutScore, 0, NoteScoreDefaults.maxAfterCutScore, 0),
    SliderHead(NoteScoreDefaults.maxCenterDistanceCutScore, 0, NoteScoreDefaults.maxBeforeCutScore, NoteScoreDefaults.maxAfterCutScore, NoteScoreDefaults.maxAfterCutScore, 0),
    SliderTail(NoteScoreDefaults.maxCenterDistanceCutScore, NoteScoreDefaults.maxBeforeCutScore, NoteScoreDefaults.maxBeforeCutScore, 0, NoteScoreDefaults.maxAfterCutScore, 0),
    BurstSliderHead(NoteScoreDefaults.maxCenterDistanceCutScore, 0, NoteScoreDefaults.maxBeforeCutScore, 0, 0, 0),
    BurstSliderElement(0, 0, 0, 0, 0, 20);

    fun maxCutScore() = maxCenterDistanceCutScore + maxBeforeCutScore + maxAfterCutScore + fixedCutScore
}

fun computeMaxMultipliedScoreForBeatmap(data: BSDifficultyV3): Int {
    val notes = data.colorNotes.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val sliders = data.sliders.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()
    val burstSliders = data.burstSliders.orNull()?.mapNotNull { s -> s.orNull() } ?: listOf()

    val slidersByBeat = sliders.groupBy { it.time }
    val slidersByTailBeat = sliders.groupBy { it.tailTime }
    val burstSlidersByBeat = burstSliders.groupBy { it.time }

    val noteItems = notes
        .map {
            val matchesHead = slidersByBeat[it.time]?.any { s -> it.color == s.color && it.x == s.x && it.y == s.y } == true
            val matchesTail = slidersByTailBeat[it.time]?.any { s -> it.color == s.color && it.x == s.tailX && it.y == s.tailY } == true
            val matchesBurst = burstSlidersByBeat[it.time]?.any { s -> it.color == s.color && it.x == s.x && it.y == it.y } == true

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

    val burstItems = burstSliders.flatMap {
        val sliceCount = it.sliceCount.or(0)
        (1 until sliceCount).map { i ->
            val t = i / (sliceCount - 1).toFloat()
            val beat = (it.time + (it.tailTime - it.time) * t)
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
