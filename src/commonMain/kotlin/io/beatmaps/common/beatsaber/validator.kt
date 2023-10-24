package io.beatmaps.common.beatsaber

import kotlinx.serialization.Serializable

@Serializable
data class BMPropertyInfo(val name: String, val descriptor: String? = null, val index: Int? = null)
