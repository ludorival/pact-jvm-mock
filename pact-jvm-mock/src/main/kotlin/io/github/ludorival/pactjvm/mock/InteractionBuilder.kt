package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper


class InteractionBuilder {

    internal lateinit var currentObjectMapper: ObjectMapper
    private var description: InteractionHandler<String?> =  { PactMock.currentTestName }
    private val requestMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val responseMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private var providerStatesHandler: (ProviderStateBuilder.(Pact.Interaction) -> ProviderStateBuilder)?  = null
    fun description(description: InteractionHandler<String>) = apply { this.description = description }

    fun providerState(block: ProviderStateBuilder.(Pact.Interaction) -> ProviderStateBuilder) = apply {
        providerStatesHandler = block
    }

    fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction {
        val providerStateBuilder = ProviderStateBuilder()
        return Pact.Interaction(request = request, response = response).run {
            copy(description = description(this) ?: PactMock.currentTestName ?: "${request.method} ${request.path}?${request.query ?: ""} returns ${response.status}")
        }.run {
            providerStatesHandler?.invoke(providerStateBuilder, this)
            copy(providerStates = providerStateBuilder.get().ifEmpty { null })
         }.copy(
            request = request.copy(matchingRules = requestMatchingRulesBuilder.build()),
            response = response.copy(matchingRules = responseMatchingRulesBuilder.build())
            )

    }

    fun responseMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
        responseMatchingRulesBuilder.apply { block() }
    }

    fun requestMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
        requestMatchingRulesBuilder.apply { block() }
    }

    inner class ProviderStateBuilder {

        val providerStates = mutableListOf<Pact.Interaction.ProviderState>()
        fun state(state: String, params: Map<String, Any?>? = null): ProviderStateBuilder = apply {
            providerStates.add(Pact.Interaction.ProviderState(state, params))
        }

        internal fun get(): List<Pact.Interaction.ProviderState> = providerStates.toList()
    }

}
