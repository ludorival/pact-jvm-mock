package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.annotation.JsonInclude

inline fun <reified T> serializerWith(crossinline supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
        supplier(gen)
    }
}

inline fun <reified T> serializerAsDefault(defaultValue: String) =
    serializerWith<T> { it.writeString(defaultValue) }


fun Pact.Interaction.getConsumerName() = request.path.split("/").first { it.isNotBlank() }


internal val DEFAULT_OBJECT_MAPPER = ObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }

