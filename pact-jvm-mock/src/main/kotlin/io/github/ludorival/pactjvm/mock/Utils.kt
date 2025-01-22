package io.github.ludorival.pactjvm.mock

import au.com.dius.pact.core.model.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

fun RequestResponseInteraction.getConsumerName() = request.path.split("/").first { it.isNotBlank() }

fun <E: Any> anError(error: E) = PactMockResponseError(error)

fun <P: BasePact> getCurrentPact(consumerName: String, providerName: String): P? {
    return PactMock.getCurrentPact(consumerName, providerName) as? P
}


fun clearPact(consumerName: String, providerName: String) {
    PactMock.clearPact(consumerName, providerName)
}
typealias InteractionHandler<R> = InteractionBuilder<*>.() -> R


internal val LOGGER: KLogger = KotlinLogging.logger {}