package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

fun RequestResponseInteraction.getConsumerName() = request.path.split("/").first { it.isNotBlank() }

fun <E: Any> anError(error: E) = PactMockResponseError(error)

fun getCurrentPact(providerName: String): RequestResponsePact? {
    return PactMock.getCurrentPact(providerName) as? RequestResponsePact
}

fun clearPact(providerName: String) {
    PactMock.clearPact(providerName)
}
typealias InteractionHandler<R> = InteractionBuilder<*>.() -> R


internal val LOGGER: KLogger = KotlinLogging.logger {}