package io.github.ludorival.pactjvm.mockk

data class InteractionOptions(
    var description: String? = null,
    var providerStates: List<String>? = null
)
