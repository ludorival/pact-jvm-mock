package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Call
import java.util.concurrent.ConcurrentHashMap

internal object PactMockk {


    private var pactOptions = PactOptions.DEFAULT_OPTIONS
    internal fun setPactOptions(pactOptions: PactOptions) {
        this.pactOptions = pactOptions
    }

    private val pacts: ConcurrentHashMap<String, PactToWrite> = ConcurrentHashMap()


    fun writePacts() {
        pacts.values.forEach {
            it.write(pactOptions.pactDirectory)
        }
    }


    private fun getPact(consumerName: String) = pacts.getOrPut(consumerName) {
        PactToWrite(
            pactOptions.consumer,
            ProviderMetaData(
                consumerName, pactOptions.objectMapperCustomizer.invoke(consumerName),
                pactOptions.pactMetaData
            ),
            pactOptions.isDeterministic
        )
    }

    private fun getAdapterFor(call: Call) = pactOptions.adapters.find { it.support(call) }
    private fun addInteraction(interaction: Pact.Interaction) {
        val consumerName = pactOptions.determineProviderFromInteraction.invoke(interaction)
        val pactToWrite = getPact(consumerName)
        pacts[consumerName] = pactToWrite.addInteraction(
            serializeRequestAndResponse(
                interaction,
                pactToWrite.providerMetaData.customObjectMapper
            )
        )
    }

    internal fun <T> interceptAndGet(call: Call, response: Result<T>, interactionOptions: InteractionOptions): T {
        val adapter = getAdapterFor(call)
        return if (adapter != null) {
            val interaction = adapter.buildInteraction(call, response, interactionOptions)
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
