package io.github.ludorival.pactjvm.mock

import java.net.URI

abstract class PactMockAdapter {

    abstract fun support(call: Call): Boolean

    open fun <T> buildInteraction(
        call: Call,
        result: Result<T>,
        interactionBuilder: InteractionBuilder
    ): Pact.Interaction {
        val uri = call.getUri()
        val body = call.getRequestBody()
        return interactionBuilder.build(request = Pact.Interaction.Request(
            method = call.getHttpMethod(),
            path = uri.path,
            query = uri.query,
            headers = call.getHttpHeaders(),
            body = body
        ),
        response = result.getResponse())
    }


    abstract fun Call.getUri(): URI

    abstract fun Call.getHttpMethod(): Pact.Interaction.Request.Method

    abstract fun Call.getHttpHeaders(): Map<String, String>?

    abstract fun Call.getRequestBody(): Any?

    abstract fun <T> Result<T>.getResponse(): Pact.Interaction.Response


    open fun <T> returnsResult(result: Result<T>) = result.getOrThrow()
}
