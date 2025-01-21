package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.Interaction
import java.util.concurrent.ConcurrentHashMap

internal object PactMock : CallInterceptor {

    private var pactConfiguration: PactConfiguration = object : PactConfiguration("") {}

    internal fun setPactConfiguration(config: PactConfiguration) {
        this.pactConfiguration = config
    }

    internal var currentTestName: String? = null

    private val pacts: ConcurrentHashMap<String, PactToWrite> = ConcurrentHashMap()

    fun writePacts() {
        pacts.values.forEach {
            it.write()
        }
    }

    fun clearPact(providerName: String) {
        LOGGER.debug { "Clearing pact for provider: $providerName" }
        pacts.remove(getId(providerName))
    }

    fun getCurrentPact(providerName: String) = pacts[getId(providerName)]?.pact

    private fun getId(providerName: String) = "${pactConfiguration.consumer}-$providerName-${pactConfiguration.isDeterministic()}"

    private fun getPact(providerName: String) = pacts.getOrPut(getId(providerName)) {
        LOGGER.debug { "Creating new pact for provider: $providerName" }
        PactToWrite(providerName, pactConfiguration)
    }

    private fun <I: Interaction> addInteraction(interaction: I, providerName: String) {
        LOGGER.debug { "Adding interaction for provider: $providerName, description: ${interaction.description}" }
        val pactToWrite = getPact(providerName)
        pacts[getId(providerName)] = pactToWrite.addInteraction(
            interaction
        )
    }

    override fun <T> interceptAndGet(interactionBuilder: InteractionBuilder<T>): T {
        val call = interactionBuilder.call
        if (currentTestName == null) {
            LOGGER.debug { "No test name set, skipping pact recording" }
            return call.result.getOrThrow()
        }
        val adapter = pactConfiguration.getAdapterFor(call) ?: run {
            LOGGER.debug { "No adapter found for call, skipping pact recording" }
            return call.result.getOrThrow()
        }
        val providerName = adapter.determineProvider(call)
        runCatching { adapter.buildInteraction(interactionBuilder, providerName) }
            .onFailure { LOGGER.warn { "Failed to build interaction: ${it.message}" } }
            .getOrNull()
            ?.let { addInteraction(it, providerName) }
        return adapter.returnsResult(call.result)
    }

}
