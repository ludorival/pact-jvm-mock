package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import org.slf4j.LoggerFactory

 fun <T> serializerWith( supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
        supplier(gen)
    }
}

fun < T> serializerAsDefault(defaultValue: String) =
    serializerWith<T> { it.writeString(defaultValue) }


fun Pact.Interaction.getConsumerName() = request.path.split("/").first { it.isNotBlank() }

fun <E:Any> anError(error: E) = PactMockResponseError(error)

fun getCurrentPact(providerName: String): Pact? {
    return PactMock.getCurrentPact(providerName)
}

fun clearPact(providerName: String) {
    PactMock.clearPact(providerName)
}

typealias InteractionHandler<R> = (Pact.Interaction) -> R

internal val LOGGER = LoggerFactory.getLogger(PactMock::class.java)