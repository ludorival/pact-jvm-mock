package io.github.ludorival.pactjvm.mock

interface InteractionBuilder {
    fun description(description: String): InteractionBuilder

    fun providerState(providerState: String, params: Map<String, Any?> ? = null): InteractionBuilder

    fun requestMatchingRules(block: MatchingRulesBuilder.() -> Unit): InteractionBuilder
    
    fun responseMatchingRules(block: MatchingRulesBuilder.() -> Unit): InteractionBuilder

    fun build(request: Pact.Interaction.Request, response: Pact.Interaction.Response): Pact.Interaction

}
