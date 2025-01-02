package io.github.ludorival.pactjvm.mock

@Suppress("LongParameterList")
class PactOptions private constructor(
    val consumer: String,
    val pactDirectory: String,
    val isDeterministic: Boolean,
    val determineProviderFromInteraction: DetermineProviderFromInteraction,
    val objectMapperCustomizer: ObjectMapperCustomizer,
    val pactMetaData: Pact.MetaData,
    val adapters: List<PactMockAdapter>
) {

    class Builder {
        var consumer: String = ""
        var pactDirectory: String = "./src/test/resources/pacts"
        var isDeterministic: Boolean = false
        var determineProviderFromInteraction: DetermineProviderFromInteraction = { it.getConsumerName() }
        var objectMapperCustomizer: ObjectMapperCustomizer = { null }
        var pactMetaData: Pact.MetaData = Pact.DEFAULT_METADATA
        private val adapters: MutableList<PactMockAdapter> = mutableListOf()

        fun addAdapter(adapter: PactMockAdapter) = adapters.add(adapter)

        fun build(): PactOptions = PactOptions(
            consumer,
            pactDirectory,
            isDeterministic,
            determineProviderFromInteraction,
            objectMapperCustomizer,
            pactMetaData,
            adapters
        )
    }

    companion object {
        val DEFAULT_OPTIONS = Builder().build()
    }
}
