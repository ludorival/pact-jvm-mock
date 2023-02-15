package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.annotation.JsonProperty

data class Pact(
    val provider: Pacticipant,
    val consumer: Pacticipant,
    val interactions: List<Interaction> = emptyList(),
    val metadata: MetaData,

    ) {

    constructor(provider: String, consumerMetaData: ConsumerMetaData, interactions: List<Interaction>) : this(
        Pacticipant(provider),
        Pacticipant(consumerMetaData.name),
        metadata = consumerMetaData.pactMetaData,
        interactions = interactions
    )

    data class Pacticipant(val name: String)

    data class Interaction(
        val description: String = "",

        @JsonProperty("providerStates") val providerStates: List<ProviderState>? = null,
        val request: Request,
        val response: Response
    ) {

        data class ProviderState(val name: String)
        data class Request(
            val method: Method,
            val path: String,
            val query: String? = null,
            val headers: Map<String, String>? = null,
            val body: Any? = null
        ) {
            enum class Method {
                GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
            }
        }

        data class Response(
            val body: Any?,
            val status: Int = 200,
            val headers: Map<String, String>? = null
        )
    }

    data class MetaData(
        @JsonProperty("pactSpecification") val pactSpecification: HasVersion = HasVersion("3.0.0"),
        @JsonProperty("pact-jvm") val pactJvm: HasVersion = HasVersion("4.0.10")
    ) {
        data class HasVersion(val version: String)
    }

    companion object {
        val DEFAULT_METADATA = MetaData()
    }
}
