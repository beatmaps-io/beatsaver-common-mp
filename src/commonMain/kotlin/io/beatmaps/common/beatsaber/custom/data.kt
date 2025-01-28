package io.beatmaps.common.beatsaber.custom

import io.beatmaps.common.AdditionalProperties
import io.beatmaps.common.OptionalProperty

sealed interface CustomData

interface BSCustomData<T : CustomData> {
    val customData: OptionalProperty<T?>
}

interface InfoCustomData : CustomData, AdditionalProperties {
    val contributors: OptionalProperty<List<OptionalProperty<IContributor?>>?>
    // val _editors: OptionalProperty<MapEditors?>
    // val assetBundle: OptionalProperty<Map<String, Long>>
}

interface IContributor {
    val role: OptionalProperty<String?>
    val name: OptionalProperty<String?>
    val iconPath: OptionalProperty<String?>
}

interface BSMapCustomData : CustomData {
    val time: OptionalProperty<Float?>
}

interface CustomJsonEvents : CustomData {
    val customEvents: OptionalProperty<List<OptionalProperty<CustomJsonEvent?>>?>
}

interface BSObjectCustomData : CustomData {
    val fake: OptionalProperty<Boolean?>
}

interface BSNoteCustomData : BSObjectCustomData
interface BSObstacleCustomData : BSObjectCustomData

interface DifficultyBeatmapCustomDataBase : CustomData {
    val difficultyLabel: OptionalProperty<String?>
    val information: OptionalProperty<List<OptionalProperty<String?>>?>
    val warnings: OptionalProperty<List<OptionalProperty<String?>>?>
    val suggestions: OptionalProperty<List<OptionalProperty<String?>>?>
    val requirements: OptionalProperty<List<OptionalProperty<String?>>?>
}
