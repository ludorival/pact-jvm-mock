package io.github.ludorival.pactjvm.mock

class InteractionBuilder {

    private var description: String? = null
    private val requestMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val responseMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val providerStates: MutableList<Pact.Interaction.ProviderState> = mutableListOf()
    fun description(description: String): InteractionBuilder = apply { this.description = description }

    fun providerState(block: ProviderStateBuilder.() -> ProviderStateBuilder): InteractionBuilder = apply {
        ProviderStateBuilder().run(block)
    }

    fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction {
        return Pact.Interaction(
            description = description ?: PactMock.currentTestName 
                ?: "${request.method} ${request.path}?${request.query ?: ""} returns ${response.status}",
            providerStates = providerStates.ifEmpty { null },
            request = request.copy(matchingRules = requestMatchingRulesBuilder.build()) ,
            response = response.copy(matchingRules = responseMatchingRulesBuilder.build())
        )
    }

    fun responseMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder): InteractionBuilder = apply {
        responseMatchingRulesBuilder.apply { block() }
    }

    fun requestMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder): InteractionBuilder = apply {
        requestMatchingRulesBuilder.apply { block() }
    }

    inner class ProviderStateBuilder {

        fun state(state: String, params: Map<String, Any?>? = null): ProviderStateBuilder = apply {
            providerStates.add(Pact.Interaction.ProviderState(state, params))
        }
    }

}
