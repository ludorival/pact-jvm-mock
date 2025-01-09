package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap

internal object PactMock : CallInterceptor {

    private var pactConfiguration: PactConfiguration = object : PactConfiguration("") {}

    internal fun setPactConfiguration(config: PactConfiguration) {
        this.pactConfiguration = config
    }

    internal var currentTestName: String? = null

    private val pacts: ConcurrentHashMap<String, PactToWrite> = ConcurrentHashMap()

    fun writePacts() {
        pacts.values.forEach {
            it.write()
        }
    }

    fun clearPact(providerName: String) {
        LOGGER.debug("Clearing pact for provider: {}", providerName)
        pacts.remove(getId(providerName))
    }

    fun getCurrentPact(providerName: String) = pacts[getId(providerName)]?.pact

    private fun getId(providerName: String) = "${pactConfiguration.consumer}-$providerName-${pactConfiguration.isDeterministic()}"

    private fun getPact(providerName: String) = pacts.getOrPut(getId(providerName)) {
        LOGGER.debug("Creating new pact for provider: {}", providerName)
        PactToWrite(providerName, pactConfiguration)
    }

    private fun addInteraction(interaction: Pact.Interaction) {
        val providerName = pactConfiguration.determineProviderFromInteraction(interaction)
        LOGGER.debug("Adding interaction for provider: {}, description: {}", providerName, interaction.description)
        val pactToWrite = getPact(providerName)
        pacts[getId(providerName)] = pactToWrite.addInteraction(
            serializeRequestAndResponse(
                interaction,
                pactToWrite.objectMapper
            )
        )
    }

    override fun <T> interceptAndGet(call: Call, response: Result<T>, interactionBuilder: InteractionBuilder): T {
        if (currentTestName == null) {
            LOGGER.debug("No test name set, skipping pact recording")
            return response.getOrThrow()
        }
        val adapter = pactConfiguration.getAdapterFor(call) ?: run {
            LOGGER.debug("No adapter found for call, skipping pact recording")
            return response.getOrThrow()
        }
        runCatching { adapter.buildInteraction(call, response, interactionBuilder) }
            .onFailure { LOGGER.warn("Failed to build interaction: {}", it.message) }
            .getOrNull()
            ?.let { addInteraction(it) }
        return adapter.returnsResult(response)
    }

    private fun serializeRequestAndResponse(
        interaction: Pact.Interaction,
        objectMapper: ObjectMapper
    ): Pact.Interaction {
        return interaction
            .copy(
                request = interaction.request
                    .copy(body = objectMapper.toJson(interaction.request.body)),
                response = interaction.response
                    .copy(body = objectMapper.toJson(interaction.response.body))
            )
    }

    private fun ObjectMapper.toJson(value: Any?): JsonNode? = value?.let { valueToTree(it) }
}
