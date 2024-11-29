package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.annotation.JsonProperty

data class Pact(
    val consumer: Pacticipant,
    val provider: Pacticipant,
    val interactions: List<Interaction> = emptyList(),
    val metadata: MetaData,

    ) {

    constructor(consumer: String, providerMetaData: ProviderMetaData, interactions: List<Interaction>) : this(
        Pacticipant(consumer),
        Pacticipant(providerMetaData.name),
        metadata = providerMetaData.pactMetaData,
        interactions = interactions
    )

    data class Pacticipant(val name: String)

    data class Interaction(
        val description: String = "",

        @JsonProperty("providerStates") val providerStates: List<ProviderState>? = null,
        val request: Request,
        val response: Response
    ) {

        data class ProviderState(val name: String, val params: Map<String, Any?>? = null)
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
