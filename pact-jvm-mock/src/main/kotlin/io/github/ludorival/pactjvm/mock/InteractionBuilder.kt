package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.*
import au.com.dius.pact.core.model.matchingrules.MatchingRules
import au.com.dius.pact.core.support.json.JsonValue


class InteractionBuilder<T>() {

    lateinit var call: Call<T>

    private var descriptionHandler: InteractionHandler<String?> = { PactMock.currentTestName }

    private val requestMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private val responseMatchingRulesBuilder: MatchingRulesBuilder = MatchingRulesBuilder()
    private var providerStatesHandler: (ProviderStateBuilder.() -> ProviderStateBuilder)? =
        null

    fun description(description: InteractionHandler<String>) = apply { descriptionHandler = description }

    fun providerState(block: ProviderStateBuilder.() -> ProviderStateBuilder) = apply {
        providerStatesHandler = block
    }

    data class DraftInteraction(
        val description: String,
        val providerStates: List<ProviderState>,
        val requestMatchingRules: MatchingRules,
        val responseMatchingRules: MatchingRules
    )

    fun <I : Interaction> build(
        generator: DraftInteraction.() -> I
    ): I {
        val providerStateBuilder = ProviderStateBuilder(call)
        providerStatesHandler?.invoke(providerStateBuilder)
        val draftInteraction = DraftInteraction(
            descriptionHandler(this) ?: PactMock.currentTestName ?: error("A description is required"),
            providerStateBuilder.get(), requestMatchingRulesBuilder.build(), requestMatchingRulesBuilder.build()
        )
        return generator(draftInteraction).apply {
            PactMock.currentTestName?.let { testName ->
                comments["testname"] = JsonValue.StringValue(testName)
            }
        }
    }


    fun responseMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
        responseMatchingRulesBuilder.apply { block() }
    }

    fun requestMatchingRules(block: MatchingRulesBuilder.() -> MatchingRulesBuilder) = apply {
        requestMatchingRulesBuilder.apply { block() }
    }


}
