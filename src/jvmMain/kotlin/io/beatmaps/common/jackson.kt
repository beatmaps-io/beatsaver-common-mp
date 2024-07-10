package io.beatmaps.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.beatmaps.common.api.HumanEnum
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement

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
        addSerializer(MapTag::class.java, MapTagsSerializer.INSTANCE)
        addDeserializer(MapTag::class.java, MapTagsDeserializer.INSTANCE)
        addDeserializer(LocalDate::class.java, LocalDateDeserializer.INSTANCE)
        addSerializer(JsonElement::class.java, JsonElementSerializer.INSTANCE)
    }
}

class JsonElementSerializer : StdSerializer<JsonElement>(JsonElement::class.java) {
    companion object {
        val INSTANCE: JsonElementSerializer = JsonElementSerializer()
    }

    override fun serialize(value: JsonElement?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeRaw(": " + json.encodeToString(value))
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

class MapTagsSerializer : StdSerializer<MapTag>(MapTag::class.java) {
    companion object {
        val INSTANCE: MapTagsSerializer = MapTagsSerializer()
    }

    override fun serialize(value: MapTag, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.slug)
    }
}
class MapTagsDeserializer : StdDeserializer<MapTag>(MapTag::class.java) {
    companion object {
        val INSTANCE: MapTagsDeserializer = MapTagsDeserializer()
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?) =
        p?.valueAsString?.let { MapTag.fromSlug(it) } ?: MapTag.None
}
class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
    companion object {
        val INSTANCE: LocalDateDeserializer = LocalDateDeserializer()
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?) =
        p?.valueAsString?.let { LocalDate.parse(it) }
}
