package io.github.ludorival.pactjvm.mockk

import io.mockk.Call
import java.net.URI

abstract class PactMockkAdapter {

    abstract fun support(call: Call): Boolean

    open fun <T> buildInteraction(
        call: Call,
        result: Result<T>,
        interactionOptions: InteractionOptions
    ): Pact.Interaction {
        val uri = call.getUri()
        val body = call.getRequestBody()
        return Pact.Interaction(
            description = interactionOptions.description ?: "$uri",
            providerStates = interactionOptions.providerStates?.map { Pact.Interaction.ProviderState(it) },
            request = Pact.Interaction.Request(
                method = call.getHttpMethod(),
                path = uri.path,
                query = uri.query,
                headers = call.getHttpHeaders(),
                body = body
            ),
            response = result.getResponse()
        )
    }


    abstract fun Call.getUri(): URI

    abstract fun Call.getHttpMethod(): Pact.Interaction.Request.Method

    abstract fun Call.getHttpHeaders(): Map<String, String>?

    abstract fun Call.getRequestBody(): Any?

    abstract fun <T> Result<T>.getResponse(): Pact.Interaction.Response


}
