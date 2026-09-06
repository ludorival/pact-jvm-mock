package io.github.ludorival.pactjvm.mock.spring

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

fun interface JsonBodySerializer {
    fun serialize(body: Any?): ByteArray
}

class JacksonJsonBodySerializer(private val objectMapper: ObjectMapper = defaultObjectMapper()) : JsonBodySerializer {

    override fun serialize(body: Any?): ByteArray = objectMapper.writeValueAsBytes(body)

    companion object {
        fun defaultObjectMapper(): ObjectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .build()
    }
}
