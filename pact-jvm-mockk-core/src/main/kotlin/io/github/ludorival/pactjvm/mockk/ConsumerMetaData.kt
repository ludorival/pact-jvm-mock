package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mockk.Pact.Companion.DEFAULT_METADATA

data class ConsumerMetaData(
    val name: String,
    val customObjectMapper: ObjectMapper = DEFAULT_OBJECT_MAPPER,
    val pactMetaData: Pact.MetaData = DEFAULT_METADATA
)
