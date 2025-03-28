package io.beatmaps.common.beatsaber.vivify

import io.beatmaps.common.FileLimits
import org.valiktor.Constraint

data class VivifyName(val key: String, val values: Set<String>) : Constraint
data class VivifyCrc(val bundle: String) : Constraint
data class VivifySize(val bundle: String, val size: Long, val limit: Long) : Constraint {
    val sizeInfo = FileLimits.printLimit(size, limit)
}
data class AssetsRead(val bundles: Set<String>) : Constraint
data class AssetExists(val asset: String) : Constraint
object AssetsMatch : Constraint
object HasAssets : Constraint
