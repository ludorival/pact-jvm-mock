package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.databind.ObjectMapper

abstract class PactConfiguration(val consumer: String, vararg adapters: PactMockAdapter) {
    private val adapters: List<PactMockAdapter> = adapters.toList()
    open fun getPactDirectory(): String = "./src/test/resources/pacts"
    open fun isDeterministic(): Boolean = false
    open fun determineProviderFromInteraction(interaction: Pact.Interaction): String = 
        interaction.request.path.split("/").first { it.isNotBlank() }
    open fun customizeObjectMapper(providerName: String): ObjectMapper? = null
    open fun getPactMetaData(): Pact.MetaData = Pact.DEFAULT_METADATA

    fun getAdapterFor(call: Call) = adapters.find { it.support(call) }
} 
