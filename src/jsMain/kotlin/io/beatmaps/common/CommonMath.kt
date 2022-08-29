package io.beatmaps.common

actual object CommonMath {
    actual fun fixed(n: Float, p: Int) = fixedStr(n, p).toFloat()
    actual fun fixed(n: Double, p: Int) = fixedStr(n, p).toDouble()

    actual fun fixedStr(n: Float, p: Int) = fixedStr(n.toDouble(), p)
    actual fun fixedStr(n: Double, p: Int) = intFixed(n, p) as String

    private fun intFixed(n: Double, p: Int) = n.asDynamic().toFixed(p)
}
