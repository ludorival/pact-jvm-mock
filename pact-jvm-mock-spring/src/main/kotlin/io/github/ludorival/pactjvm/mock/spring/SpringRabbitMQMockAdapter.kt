package io.github.ludorival.pactjvm.mock.spring

import au.com.dius.pact.core.model.OptionalBody
import au.com.dius.pact.core.model.ProviderState
import au.com.dius.pact.core.model.messaging.Message
import au.com.dius.pact.core.model.matchingrules.MatchingRulesImpl
import io.github.ludorival.pactjvm.mock.Call
import io.github.ludorival.pactjvm.mock.InteractionBuilder
import io.github.ludorival.pactjvm.mock.PactMockAdapter
import org.springframework.amqp.rabbit.core.RabbitTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

open class SpringRabbitMQMockAdapter(
    private val provider: String,
    private val objectMapper: ObjectMapper = defaultObjectMapper()
) : PactMockAdapter<Message>() {

    constructor(provider: String) : this(provider, defaultObjectMapper())

    override fun support(call: Call<*>): Boolean {
        return call.self is RabbitTemplate
    }

    override fun <T> buildInteraction(
        interactionBuilder: InteractionBuilder<T>,
        providerName: String
    ): Message {
        val call = interactionBuilder.call
        val exchange = call.args.firstOrNull { it is String } as? String ?: ""
        val routingKey = call.args.getOrNull(1) as? String ?: ""
        val message = call.args.lastOrNull()
        
        val messageContent = when (message) {
            is String -> message
            null -> ""
            else -> objectMapper.writeValueAsString(message)
        }
        
        return interactionBuilder.build { Message(
            description = description,
            providerStates = providerStates,
            contents = OptionalBody.body(messageContent.toByteArray()),
            metadata = mutableMapOf(
                "exchange" to exchange,
                "routing_key" to routingKey
            ),
            matchingRules = requestMatchingRules
        )
        }
    }

    override fun determineConsumerAndProvider(call: Call<*>): Pair<String, String> {
        val exchange = call.args.firstOrNull { it is String } as? String
        return Pair(exchange?.split(".")?.firstOrNull() ?: "default", provider)
    }

    companion object {
        private fun defaultObjectMapper(): ObjectMapper = ObjectMapper()
    }
} 