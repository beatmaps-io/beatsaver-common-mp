package io.beatmaps.common.zip

import java.io.InputStream
import java.nio.charset.Charset

fun readFromStream(i: InputStream) = readFromBytes(i.readAllBytes())
fun readFromBytes(b: ByteArray): String {
    val charset = detectBOMBytes(b.take(4).toByteArray())
    return String(b, charset ?: Charsets.UTF_8)
}

fun detectBOMBytes(BOMBytes: ByteArray?): Charset? {
    if (BOMBytes == null) throw IllegalArgumentException("Must provide a valid BOM byte array!")
    if (BOMBytes.size < 2) return null
    if (BOMBytes[0] == 0xff.toByte() && BOMBytes[1] == 0xfe.toByte() && (BOMBytes.size < 4 || BOMBytes[2] != 0.toByte() || BOMBytes[3] != 0.toByte())) return Charsets.UTF_16
    if (BOMBytes[0] == 0xfe.toByte() && BOMBytes[1] == 0xff.toByte()) return Charsets.UTF_16BE
    if (BOMBytes.size < 3) return null
    if (BOMBytes[0] == 0xef.toByte() && BOMBytes[1] == 0xbb.toByte() && BOMBytes[2] == 0xbf.toByte()) return Charsets.UTF_8
    if (BOMBytes.size < 4) return null
    if (BOMBytes[0] == 0xff.toByte() && BOMBytes[1] == 0xfe.toByte() && BOMBytes[2] == 0.toByte() && BOMBytes[3] == 0.toByte()) return Charsets.UTF_32
    return null
}
