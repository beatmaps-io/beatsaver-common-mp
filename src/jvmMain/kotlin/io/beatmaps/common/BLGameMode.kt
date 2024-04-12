package io.beatmaps.common

import io.beatmaps.common.api.ECharacteristic

@Suppress("EnumEntryName")
enum class BLGameMode(val idx: Int, val characteristic: ECharacteristic) {
    Standard(0, ECharacteristic.Standard), OneSaber(1, ECharacteristic.OneSaber), NoArrows(2, ECharacteristic.NoArrows),
    `90Degree`(3, ECharacteristic._90Degree), `360Degree`(4, ECharacteristic._360Degree), Lightshow(5, ECharacteristic.Lightshow),
    Lawless(6, ECharacteristic.Lawless), Legacy(7, ECharacteristic.Legacy);
}