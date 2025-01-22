package io.github.ludorival.pactjvm.mock.test

import au.com.dius.pact.core.model.PactSpecVersion
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.Call
import io.github.ludorival.pactjvm.mock.spring.SpringRabbitMQMockAdapter
import io.github.ludorival.pactjvm.mock.test.shoppingservice.objectMapperBuilder

object RabbitMQPactV4Config : PactConfiguration(
    object : SpringRabbitMQMockAdapter("order-service", objectMapperBuilder().build()) {
        override fun determineConsumerAndProvider(call: Call<*>): Pair<String, String> {
            return "order-service" to "shopping-list"
        }
    }
) {
    override fun getPactVersion(): PactSpecVersion = PactSpecVersion.V4
} 