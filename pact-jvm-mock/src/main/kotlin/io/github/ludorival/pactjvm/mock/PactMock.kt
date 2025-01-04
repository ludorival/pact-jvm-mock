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
        pacts.remove(getId(providerName))
    }

    fun getCurrentPact(providerName: String) = pacts[getId(providerName)]?.pact

    private fun getId(providerName: String) = "${pactConfiguration.consumer}-$providerName-${pactConfiguration.isDeterministic()}"

    private fun getPact(providerName: String) = pacts.getOrPut(getId(providerName)) {
        PactToWrite(providerName, pactConfiguration)
    }

    private fun addInteraction(interaction: Pact.Interaction) {
        val providerName = pactConfiguration.determineProviderFromInteraction(interaction)
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
            return response.getOrThrow()
        }
        val adapter = pactConfiguration.getAdapterFor(call)
        return if (adapter != null) {
            val interaction = adapter.buildInteraction(call, response, interactionBuilder)
            addInteraction(interaction)
            adapter.returnsResult(response)
        } else response.getOrThrow()
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
