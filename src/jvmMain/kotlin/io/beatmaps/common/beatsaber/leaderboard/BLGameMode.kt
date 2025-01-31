package io.beatmaps.common.beatsaber.leaderboard

import io.beatmaps.common.api.ECharacteristic
import kotlinx.serialization.SerialName

enum class BLGameMode(val idx: Int, val characteristic: ECharacteristic) {
    Standard(0, ECharacteristic.Standard),
    OneSaber(1, ECharacteristic.OneSaber),
    NoArrows(2, ECharacteristic.NoArrows),

    @SerialName("90Degree")
    Rotation90Degree(3, ECharacteristic.Rotation90Degree),

    @SerialName("360Degree")
    Rotation360Degree(4, ECharacteristic.Rotation360Degree),

    Lightshow(5, ECharacteristic.Lightshow),
    Lawless(6, ECharacteristic.Lawless),
    Legacy(7, ECharacteristic.Legacy)
}
