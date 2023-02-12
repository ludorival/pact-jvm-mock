package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

data class PactWithObjectMapper(val pact: Pact, private val objectMapper: ObjectMapper = DEFAULT_OBJECT_MAPPER) {

    constructor(provider: String, consumerMetaData: ConsumerMetaData) : this(
        Pact(provider, consumerMetaData),
        consumerMetaData.customObjectMapper
    )

    val pactFile get() = "${pact.consumer.name}-${pact.provider.name}.json"
    internal fun write(directory: String) {
        if (pact.interactions.isEmpty()) return
        File(directory, pactFile).apply {
            val path = Paths.get(parentFile.path)
            if (!exists()) {
                Files.createDirectories(path)
            }
            val json = objectMapper.valueToTree<JsonNode>(pact)
            writeText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json))
        }
    }
}
