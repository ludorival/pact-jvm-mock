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
            pactOptions.provider,
            ConsumerMetaData(
                consumerName, pactOptions.objectMapperCustomizer.invoke(consumerName),
                pactOptions.pactMetaData
            ),
            pactOptions.isDeterministic
        )
    }

    private fun getAdapterFor(call: Call) = pactOptions.adapters.find { it.support(call) }
    private fun addInteraction(interaction: Pact.Interaction) {
        val consumerName = pactOptions.determineConsumerFromInteraction.invoke(interaction)
        val pactToWrite = getPact(consumerName)
        pacts[consumerName] = pactToWrite.addInteraction(
            serializeRequestAndResponse(
                interaction,
                pactToWrite.consumerMetaData.customObjectMapper
            )
        )
    }

    internal fun <T> intercept(call: Call, response: Result<T>, interactionOptions: InteractionOptions) {
        val adapter = getAdapterFor(call)
        if (adapter != null) {
            val interaction = adapter.buildInteraction(call, response, interactionOptions)
            addInteraction(interaction)
        }
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
