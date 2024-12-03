package io.github.ludorival.pactjvm.mockk

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mockk.Pact.Companion.DEFAULT_METADATA

data class ProviderMetaData(
    val name: String,
    val customObjectMapper: ObjectMapper? = null,
    val pactMetaData: Pact.MetaData = DEFAULT_METADATA
)
