package io.beatmaps.common

import io.beatmaps.common.api.ECharacteristic

enum class SSGameMode(val idx: Int, val characteristic: ECharacteristic) {
    SoloStandard(0, ECharacteristic.Standard), SoloOneSaber(1, ECharacteristic.OneSaber), SoloNoArrows(2, ECharacteristic.NoArrows),
    Solo90Degree(3, ECharacteristic._90Degree), Solo360Degree(4, ECharacteristic._360Degree), SoloLightshow(5, ECharacteristic.Lightshow),
    SoloLawless(6, ECharacteristic.Lawless);

    companion object {
        private val map = values().associateBy(SSGameMode::idx)
        fun fromInt(type: Int) = map[type]
    }
}
