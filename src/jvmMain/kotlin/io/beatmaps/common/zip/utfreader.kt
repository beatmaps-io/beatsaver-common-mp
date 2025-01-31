package io.beatmaps.common.zip

import java.io.InputStream
import java.nio.charset.Charset

fun readFromStream(i: InputStream) = readFromBytes(i.readAllBytes())
fun readFromBytes(b: ByteArray): String {
    val charset = detectBOMBytes(b.take(4).toByteArray())
    return String(b, charset ?: Charsets.UTF_8)
}

fun detectBOMBytes(bOMBytes: ByteArray?): Charset? {
    if (bOMBytes == null) throw IllegalArgumentException("Must provide a valid BOM byte array!")
    if (bOMBytes.size < 2) return null
    if (bOMBytes[0] == 0xff.toByte() && bOMBytes[1] == 0xfe.toByte() && (bOMBytes.size < 4 || bOMBytes[2] != 0.toByte() || bOMBytes[3] != 0.toByte())) return Charsets.UTF_16
    if (bOMBytes[0] == 0xfe.toByte() && bOMBytes[1] == 0xff.toByte()) return Charsets.UTF_16BE
    if (bOMBytes.size < 3) return null
    if (bOMBytes[0] == 0xef.toByte() && bOMBytes[1] == 0xbb.toByte() && bOMBytes[2] == 0xbf.toByte()) return Charsets.UTF_8
    if (bOMBytes.size < 4) return null
    if (bOMBytes[0] == 0xff.toByte() && bOMBytes[1] == 0xfe.toByte() && bOMBytes[2] == 0.toByte() && bOMBytes[3] == 0.toByte()) return Charsets.UTF_32
    return null
}
