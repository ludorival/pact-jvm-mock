package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider

inline fun <reified T> serializerWith(crossinline supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
    override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
        supplier(gen)
    }
}

inline fun <reified T> serializerAsDefault(defaultValue: String) =
    serializerWith<T> { it.writeString(defaultValue) }

fun pactOptions(builder: PactOptions.Builder.() -> Unit) =
    PactOptions.Builder().apply(builder).build()

fun Pact.Interaction.getConsumerName() = request.path.split("/").first { it.isNotBlank() }

fun state(name: String, params: Map<String, Any?>? = null) = Pact.Interaction.ProviderState(name, params)

fun <E:Any> anError(error: E) = PactMockResponseError(error)

fun getCurrentPact(providerName: String): Pact? {
    return PactMock.getCurrentPact(providerName)
}

fun clearPact(providerName: String) {
    PactMock.clearPact(providerName)
}

typealias DetermineProviderFromInteraction = (Pact.Interaction) -> String

typealias ObjectMapperCustomizer = (String) -> ObjectMapper?