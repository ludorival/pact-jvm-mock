package io.github.ludorival.pactjvm.mockk

@Suppress("LongParameterList")
class PactOptions private constructor(
    val consumer: String,
    val pactDirectory: String,
    val isDeterministic: Boolean,
    val determineProviderFromInteraction: DetermineProviderFromInteraction,
    val objectMapperCustomizer: ObjectMapperCustomizer,
    val pactMetaData: Pact.MetaData,
    val adapters: List<PactMockkAdapter>
) {

    class Builder {
        var consumer: String = ""
        var pactDirectory: String = "./src/test/resources/pacts"
        var isDeterministic: Boolean = false
        var determineProviderFromInteraction: DetermineProviderFromInteraction = { it.getConsumerName() }
        var objectMapperCustomizer: ObjectMapperCustomizer = { DEFAULT_OBJECT_MAPPER }
        var pactMetaData: Pact.MetaData = Pact.DEFAULT_METADATA
        private val adapters: MutableList<PactMockkAdapter> = mutableListOf()

        fun addAdapter(adapter: PactMockkAdapter) = adapters.add(adapter)

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
