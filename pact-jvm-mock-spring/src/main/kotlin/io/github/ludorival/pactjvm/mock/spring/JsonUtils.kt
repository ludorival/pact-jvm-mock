package io.github.ludorival.pactjvm.mock.spring

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider


fun <T> serializerWith(supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
        supplier(gen)
    }
}

fun <T> serializerAsDefault(defaultValue: String) =
    serializerWith<T> { it.writeString(defaultValue) }
