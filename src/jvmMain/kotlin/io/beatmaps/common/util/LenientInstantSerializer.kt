package io.beatmaps.common.util

import io.beatmaps.common.OptionalProperty
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

object LenientInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant =
        decoder.decodeString().let {
            try {
                Instant.parse(it)
            } catch (e: IllegalArgumentException) {
                LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC)
            }
        }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

class BadParameter(val paramName: String, val paramType: KClass<*>) : Throwable()

data class ParamInfo<T, V : OptionalProperty<O>?, O : Any>(val parent: T, val param: KProperty1<T, V>, val paramType: KClass<O>) {
    fun require() {
        val value = param.get(parent)
        if (value is OptionalProperty.WrongType) {
            throw BadParameter(param.name, paramType)
        }
    }
}
inline fun <T, V : OptionalProperty<O>?, reified O : Any> T.paramInfo(param: KProperty1<T, V>) = ParamInfo(this, param, O::class)
fun requireParams(vararg info: ParamInfo<*, *, *>) {
    info.forEach {
        it.require()
    }
}
