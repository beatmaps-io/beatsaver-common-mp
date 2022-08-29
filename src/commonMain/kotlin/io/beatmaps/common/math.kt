package io.beatmaps.common

import kotlin.math.pow

expect object CommonMath {
    fun fixed(n: Float, p: Int): Float
    fun fixed(n: Double, p: Int): Double
    fun fixedStr(n: Float, p: Int): String
    fun fixedStr(n: Double, p: Int): String
}

fun Float.fixed(n: Int) = CommonMath.fixed(this, n)
fun Double.fixed(n: Int) = CommonMath.fixed(this, n)
fun Float.fixedStr(n: Int) = CommonMath.fixedStr(this, n)
fun Double.fixedStr(n: Int) = CommonMath.fixedStr(this, n)
fun Int.pow(m: Int) = this.toDouble().pow(m).toLong()
