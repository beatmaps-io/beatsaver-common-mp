package io.beatmaps.common.zip

import io.beatmaps.common.beatsaber.info.BaseMapInfo
import io.beatmaps.common.beatsaber.map.BSDiff
import java.io.File

interface IMapScorer {
    fun scoreMap(infoFile: BaseMapInfo, audio: File, block: (String) -> BSDiff): Short
}
interface IMapScorerProvider {
    fun create(): IMapScorer
}
class RarException : ZipHelperException("")
open class ZipHelperException(val msg: String) : RuntimeException()
