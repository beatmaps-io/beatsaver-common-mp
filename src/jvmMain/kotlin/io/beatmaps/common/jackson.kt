package io.beatmaps.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.beatmaps.common.api.HumanEnum
import kotlinx.datetime.Instant

val inlineJackson: ObjectMapper = jacksonObjectMapper()
    .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(KotlinTimeModule())
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

val jackson: ObjectMapper = jacksonObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(KotlinTimeModule())
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

class KotlinTimeModule : SimpleModule() {

    init {
        addSerializer(Instant::class.java, InstantSerializer.INSTANCE)
        addSerializer(Float::class.java, FloatSerializer.INSTANCE)
        addSerializer(HumanEnum::class.java, HumanEnumSerializer.INSTANCE)
    }
}

class BSPrettyPrinter : DefaultPrettyPrinter() {
    init {
        _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
    }

    override fun createInstance(): BSPrettyPrinter {
        return BSPrettyPrinter()
    }

    override fun writeEndArray(gen: JsonGenerator, nrOfValues: Int) {
        if (!_arrayIndenter.isInline) {
            --_nesting
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(gen, _nesting)
        }
        gen.writeRaw(']')
    }

    override fun writeObjectFieldValueSeparator(jg: JsonGenerator) {
        jg.writeRaw(": ")
    }
}

class InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    companion object {
        val INSTANCE: InstantSerializer = InstantSerializer()
    }

    override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

class FloatSerializer : StdSerializer<Float>(Float::class.java) {
    companion object {
        val INSTANCE: FloatSerializer = FloatSerializer()
    }

    override fun serialize(value: Float, gen: JsonGenerator, provider: SerializerProvider) {
        if (value % 1 == 0f) {
            gen.writeNumber(value.toInt())
        } else {
            gen.writeNumber(value)
        }
    }
}

class HumanEnumSerializer : StdSerializer<HumanEnum<*>>(HumanEnum::class.java) {
    companion object {
        val INSTANCE: HumanEnumSerializer = HumanEnumSerializer()
    }

    override fun serialize(value: HumanEnum<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.enumName())
    }
}
