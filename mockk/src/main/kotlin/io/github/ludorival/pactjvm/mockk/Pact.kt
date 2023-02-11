package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import io.github.ludorival.pactjvm.mockk.Contract
import io.mockk.CapturingSlot
import io.mockk.MockKMatcherScope
import io.mockk.MockKStubScope
import io.mockk.slot
import org.junit.jupiter.api.extension.ExtensionContext
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class Pact(private val extensionContext: ExtensionContext) {

//    infix fun <T> registerInteraction(
//        block: Builder.() -> MockKStubScope<T, *>
//    ): StubInteraction<T> {
//        val builder = Builder()
//        val mockKStubScope = block(builder)
//        return StubInteraction(builder, mockKStubScope)
//    }

    fun setPactDirectory(directory: String) {
        extensionContext.root.getStore(NAMESPACE).put("pact-directory", directory)
    }

    fun setDefaultObjectMapper(objectMapper: ObjectMapper) {
        extensionContext.root.getStore(NAMESPACE).put("pact-object-mapper", objectMapper)
    }
//
//    inner class Builder {
//        lateinit var consumer: String
//        lateinit var provider: String
//        var description: String = extensionContext.displayName
//        var metadata: Contract.MetaData = Contract.DEFAULT_METADATA
//        val providerStates: MutableList<Contract.Interaction.ProviderState> = mutableListOf()
//        var objectmapper: ObjectMapper = extensionContext.getDefaultObjectMapper()
//
//        private val slotHttpMethod by lazy { slot<HttpMethod>() }
//        private val slotHttpEntity by lazy { slot<HttpEntity<Any>>() }
//        private val slotUrl by lazy { slot<String>() }
//        fun providerState(name: String) = providerStates.add(Contract.Interaction.ProviderState(name))
//
//        fun currentProviderStates() = providerStates.takeIf { it.isNotEmpty() }
//        fun MockKMatcherScope.captureMethod(): HttpMethod = capture(slotHttpMethod)
//        fun MockKMatcherScope.captureUrl() = and(capture(slotUrl), match { it.contains(provider) })
//        fun MockKMatcherScope.captureHttpEntity() = capture(slotHttpEntity)
//        private fun <T : Any> CapturingSlot<T>.capturedOrNull() = if (isCaptured) captured else null
//        fun <T> buildRequest(): Contract.Interaction.Request<T> {
//            val url = URI.create(
//                slotUrl.capturedOrNull()
//                    ?: error("The path should be capture, make sure you have added `capturePath`")
//            )
//            return Contract.Interaction.Request(
//                method = slotHttpMethod.captured,
//                path = url.path,
//                query = url.query,
//                headers = slotHttpEntity.capturedOrNull()?.headers?.toSingleValueMap(),
//                body = (slotHttpEntity.capturedOrNull()?.body) as T
//            )
//        }
//    }
//
//    inner class StubInteraction<T>(
//        private val builder: Builder,
//        private val stubScope: MockKStubScope<T, *>
//    ) {
//
//        infix fun  willRespondWith(block: StubInteraction<T>.(Contract.Interaction.Request<T>) -> T) {
//            stubScope answers {
//                val request = builder.buildRequest<T>()
//                val response = block(request)
//                val pactToWrites = extensionContext.getPactToWrites()
//                val contract = pactToWrites.getContract(builder)
//                contract.addInteraction(
//                    Contract.Interaction(
//                        description = builder.description,
//                        builder.currentProviderStates(),
//                        request,
//                        response.asResponse()
//                    )
//                )
//                extensionContext.savePactToWrites(pactToWrites)
//                response
//            }
//        }
//
//        infix fun willReturn(response: T) = willRespondWith { response }
//    }

    data class PactToWrites(private val values: MutableMap<String, Contract> = ConcurrentHashMap()) {
//        fun getContract(builder: Builder): Contract {
//            return with(builder) {
//                values.get("$consumer-$provider")
//                    ?: return Contract(consumer, provider, metadata, objectmapper).also {
//                        values.put("$consumer-$provider", it)
//                    }
//            }
//        }

        internal fun write(directory: String) {
            values.forEach { (_, contract) ->
                contract.write(directory)
            }
        }
    }

    companion object {
        val NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("pact-mockk")

        fun ExtensionContext.getDefaultObjectMapper() =
            root.getStore(NAMESPACE).getOrDefault("pact-object-mapper", ObjectMapper::class.java, ObjectMapper())

        fun ExtensionContext.getPactDirectory() =
            root.getStore(NAMESPACE).getOrDefault("pact-directory", String::class.java, "./pact")

        private fun ExtensionContext.getPactToWrites() = root.getStore(NAMESPACE).getOrDefault(
            "pactsToWrite", PactToWrites::class.java,
            PactToWrites()
        )

        internal fun ExtensionContext.writePacts() {
            getPactToWrites().write(getPactDirectory())
        }

        private fun ExtensionContext.savePactToWrites(pactToWrites: PactToWrites) {
            root.getStore(NAMESPACE).put("pactsToWrite", pactToWrites)
        }

        inline fun <reified T> serializerWith(crossinline supplier: (JsonGenerator) -> Unit) = object : JsonSerializer<T>() {
            override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) {
                supplier(gen)
            }
        }

        inline fun <reified T> serializerAsDefault(defaultValue: String) =
            serializerWith<T> { it.writeString(defaultValue) }
    }
}
