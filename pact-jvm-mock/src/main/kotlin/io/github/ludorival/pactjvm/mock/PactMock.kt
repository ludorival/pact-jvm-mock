package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap

internal object PactMock: CallInterceptor {


    private var pactOptions = PactOptions.DEFAULT_OPTIONS
    internal fun setPactOptions(pactOptions: PactOptions) {
        this.pactOptions = pactOptions
    }

    internal var currentTestName: String? = null

    private val pacts: ConcurrentHashMap<String, PactToWrite> = ConcurrentHashMap()


    fun writePacts() {
        pacts.values.forEach {
            it.write()
        }
    }


    fun clearPact(providerName: String) {
        pacts.remove(pactOptions.id(providerName))
    }

    fun getCurrentPact(providerName: String) = pacts.get(pactOptions.id(providerName))?.pact

    private fun getPact(providerName: String) = pacts.getOrPut(pactOptions.id(providerName)) {
        PactToWrite(providerName, pactOptions)
    }

    private fun getAdapterFor(call: Call) = pactOptions.adapters.find { it.support(call) }
    private fun addInteraction(interaction: Pact.Interaction) {
        val providerName = pactOptions.determineProviderFromInteraction.invoke(interaction)
        val pactToWrite = getPact(providerName)
        pacts[pactToWrite.id] = pactToWrite.addInteraction(
            serializeRequestAndResponse(
                interaction,
                pactToWrite.objectMapper
            )
        )
    }

    override fun <T> interceptAndGet(call: Call, response: Result<T>, interactionBuilder: InteractionBuilder): T {
        val adapter = getAdapterFor(call)
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
