package io.beatmaps.common

import java.math.BigDecimal
import java.math.RoundingMode

actual object CommonMath {
    actual fun fixed(n: Float, p: Int) = intFixed(n.toDouble(), p).toFloat()
    actual fun fixed(n: Double, p: Int) = intFixed(n, p).toDouble()

    actual fun fixedStr(n: Float, p: Int) = fixedStr(n.toDouble(), p)
    actual fun fixedStr(n: Double, p: Int): String = intFixed(n, p).toPlainString()

    private fun intFixed(n: Double, p: Int) = BigDecimal(n).setScale(p, RoundingMode.HALF_UP)
}
