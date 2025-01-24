package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.Interaction
import java.util.concurrent.ConcurrentHashMap

data class TestInfo(
    val testFileName: String,
    val methodName: String,
    val displayName: String
)

internal object PactMock : CallInterceptor {

    private var pactConfiguration: PactConfiguration = object : PactConfiguration() {}

    internal fun setPactConfiguration(config: PactConfiguration) {
        this.pactConfiguration = config
    }

    internal var currentTestInfo: TestInfo? = null

    private val pacts: ConcurrentHashMap<String, PactToWrite> = ConcurrentHashMap()

    fun writePacts() {
        pacts.values.forEach {
            it.write()
        }
    }

    fun clearPact(consumerName: String, providerName: String) {
        LOGGER.debug { "Clearing pact for provider: $providerName" }
        pacts.remove(getId(consumerName, providerName))
    }

    fun getCurrentPact(consumerName: String, providerName: String) = pacts[getId(consumerName, providerName)]?.pact

    private fun getId(consumerName: String, providerName: String) = "${consumerName}-$providerName-${pactConfiguration.isDeterministic()}"

    private fun getPact(consumerName: String, providerName: String) = pacts.getOrPut(getId(consumerName, providerName)) {
        LOGGER.debug { "Creating new pact for consumer: $consumerName, provider: $providerName" }
        PactToWrite(consumerName, providerName, pactConfiguration)
    }

    private fun <I: Interaction> addInteraction(interaction: I, consumerName: String, providerName: String) {
        LOGGER.debug { "Adding interaction for provider: $providerName, description: ${interaction.description}" }
        val pactToWrite = getPact(consumerName, providerName)
        pacts[getId(consumerName, providerName)] = pactToWrite.addInteraction(
            interaction
        )
    }

    override fun <T> interceptAndGet(interactionBuilder: InteractionBuilder<T>): T {
        val call = interactionBuilder.call
        if (currentTestInfo == null) {
            LOGGER.debug { "No test info set, skipping pact recording" }
            return call.result.getOrThrow()
        }
        val adapter = pactConfiguration.getAdapterFor(call) ?: run {
            LOGGER.debug { "No adapter found for call, skipping pact recording" }
            return call.result.getOrThrow()
        }
        val (consumerName, providerName) = adapter.determineConsumerAndProvider(call)
        runCatching { adapter.buildInteraction(interactionBuilder, providerName) }
            .onFailure { LOGGER.warn { "Failed to build interaction: ${it.message}" } }
            .getOrNull()
            ?.let { addInteraction(it, consumerName, providerName) }
        return adapter.returnsResult(call.result, providerName)
    }

}
