package io.github.ludorival.pactjvm.mockk

class InteractionBuilderImpl : InteractionBuilder {

    private var description: String? = null
    private val providerStates: MutableList<Pact.Interaction.ProviderState> = mutableListOf()
    override fun description(description: String): InteractionBuilder = apply { this.description = description }

    override fun providerState(providerState: String, params: Map<String, Any?>?): InteractionBuilder = apply {
        providerStates.add(Pact.Interaction.ProviderState(providerState, params))
    }

    override fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction {
        return Pact.Interaction(
            description = description
                ?: "${request.method} ${request.path}?${request.query ?: ""} returns ${response.status}",
            providerStates = providerStates.ifEmpty { null },
            request = request,
            response = response
        )
    }

}