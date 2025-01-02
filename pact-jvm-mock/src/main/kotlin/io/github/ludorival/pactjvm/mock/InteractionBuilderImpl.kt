package io.github.ludorival.pactjvm.mock

class InteractionBuilderImpl : InteractionBuilder {

    private var description: String? = null
    private val requestMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val responseMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val providerStates: MutableList<Pact.Interaction.ProviderState> = mutableListOf()
    override fun description(description: String): InteractionBuilder = apply { this.description = description }

    override fun providerState(providerState: String, params: Map<String, Any?>?): InteractionBuilder = apply {
        providerStates.add(Pact.Interaction.ProviderState(providerState, params))
    }

    override fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction {
        return Pact.Interaction(
            description = description ?: PactMock.currentTestName 
                ?: "${request.method} ${request.path}?${request.query ?: ""} returns ${response.status}",
            providerStates = providerStates.ifEmpty { null },
            request = request.copy(matchingRules = requestMatchingRulesBuilder.build()) ,
            response = response.copy(matchingRules = responseMatchingRulesBuilder.build())
        )
    }

    override fun responseMatchingRules(block: MatchingRulesBuilder.() -> Unit): InteractionBuilder = apply {
        responseMatchingRulesBuilder.apply(block)
    }

    override fun requestMatchingRules(block: MatchingRulesBuilder.() -> Unit): InteractionBuilder = apply {
        requestMatchingRulesBuilder.apply(block)
    }

}