package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.*
import java.net.URI
import java.nio.charset.StandardCharsets

abstract class PactMockAdapter<I: Interaction> {

    abstract fun support(call: Call<*>): Boolean

    abstract fun <T> buildInteraction(
        interactionBuilder: InteractionBuilder<T>,
        providerName: String
    ): I

    abstract fun determineProvider(call: Call<*>): String

    open fun <T> returnsResult(result: Result<T>) = result.getOrThrow()

}

