package io.github.ludorival.pactjvm.mockk

interface InteractionBuilder {
    fun description(description: String): InteractionBuilder

    fun providerState(providerState: String, params: Map<String, Any?> ? = null): InteractionBuilder


    fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction
}