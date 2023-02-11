package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

data class Contract(
    val provider: Pacticipant,
    val consumer: Pacticipant,
    val interactions: MutableSet<Interaction> = mutableSetOf(),
    val metadata: MetaData,

) {
    private val objectMapper: ObjectMapper = ObjectMapper()
    constructor(consumer: String, provider: String, metadata: MetaData = MetaData()) : this(
        Pacticipant(consumer),
        Pacticipant(provider),
        metadata = metadata,
    )

    private val pactFile get() = "${consumer.name}-${provider.name}.json"

    fun <T, R> addInteraction(interaction: Interaction): Boolean {
        return interactions.add(interaction)
    }

    fun write(directory: String) {
        val contract = this
        if (contract.interactions.isEmpty()) return
        File(directory, pactFile).apply {
            val path = Paths.get(parentFile.path)
            if (!exists()) {
                Files.createDirectories(path)
            }
            val json = objectMapper.valueToTree<JsonNode>(contract)
            writeText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json))
        }
    }

    data class Pacticipant(val name: String)

    data class Interaction(
        val description: String,

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
            val body: JsonNode? = null
        ) {
            enum class Method {
                GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
            }
        }

        data class Response(
            val body: JsonNode?,
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
