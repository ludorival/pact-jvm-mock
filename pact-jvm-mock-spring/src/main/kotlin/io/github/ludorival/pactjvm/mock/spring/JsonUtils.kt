package io.github.ludorival.pactjvm.mock.spring

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

fun <T> serializerWith(supplier: (JsonGenerator) -> Unit) = object : ValueSerializer<T>() {
    override fun serialize(value: T, gen: JsonGenerator, context: SerializationContext) {
        supplier(gen)
    }
}

fun <T> serializerAsDefault(defaultValue: String) =
    serializerWith<T> { it.writeString(defaultValue) }
