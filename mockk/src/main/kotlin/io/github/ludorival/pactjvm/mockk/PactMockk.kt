package io.github.ludorival.pactjvm.mockk

import io.mockk.Call
import java.util.concurrent.ConcurrentHashMap

object PactMockk {

    var pactDirectory: String = "./src/test/resources/pacts"

    lateinit var provider: String

    private val pacts: ConcurrentHashMap<String, PactWithObjectMapper> = ConcurrentHashMap()

    private val adapters: MutableList<PactMockkAdapter> = mutableListOf()

    fun addAdapter(adapter: PactMockkAdapter) = adapters.add(adapter)

    fun writePacts() {
        pacts.values.forEach {
            it.write(pactDirectory)
        }
    }


    private fun getAdapterFor(call: Call) = adapters.find { it.support(call) }
    private fun addInteraction(consumerMetaData: ConsumerMetaData, interaction: Pact.Interaction) {
        val pactWithObjectMapper =
            pacts.getOrPut(consumerMetaData.name) { PactWithObjectMapper(provider, consumerMetaData) }
        pacts[consumerMetaData.name] = pactWithObjectMapper.addInteraction(interaction)
    }

    internal fun <T> intercept(call: Call, response: T) {
        val adapter = getAdapterFor(call)
        if (adapter != null) {
            val (consumerMetaData, interaction) = adapter.buildInteraction(call, response)
            addInteraction(
                consumerMetaData,
                interaction.copy(
                    description = interaction.description.ifEmpty {
                        ExtractInteractionDescription.getDescriptionFromStackTrace()
                            ?: interaction.description
                    }
                )
            )
        }
    }
}
