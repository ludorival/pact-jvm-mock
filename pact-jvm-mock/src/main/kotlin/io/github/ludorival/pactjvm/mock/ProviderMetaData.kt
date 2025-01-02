package io.github.ludorival.pactjvm.mock

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mock.Pact.Companion.DEFAULT_METADATA

data class ProviderMetaData(
    val name: String,
    val customObjectMapper: ObjectMapper? = null,
    val pactMetaData: Pact.MetaData = DEFAULT_METADATA
)
