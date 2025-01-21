package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.PactSpecVersion

data class ProviderMetaData(
    val name: String,
    val pactMetaData: PactSpecVersion = PactSpecVersion.V3
)
