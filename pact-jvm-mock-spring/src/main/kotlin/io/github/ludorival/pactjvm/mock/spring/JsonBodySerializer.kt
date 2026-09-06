package io.github.ludorival.pactjvm.mock.spring

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

/**
 * Serializes an HTTP body into the JSON bytes recorded in the Pact contract.
 *
 * Implement it to plug any JSON library; [JacksonJsonBodySerializer] is the default implementation.
 */
fun interface JsonBodySerializer {
    fun serialize(body: Any?): ByteArray
}

/**
 * [JsonBodySerializer] backed by a Jackson 3 [ObjectMapper].
 */
class JacksonJsonBodySerializer(private val objectMapper: ObjectMapper = defaultObjectMapper()) : JsonBodySerializer {

    override fun serialize(body: Any?): ByteArray = objectMapper.writeValueAsBytes(body)

    companion object {
        fun defaultObjectMapper(): ObjectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .build()
    }
}
