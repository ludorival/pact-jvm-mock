package io.github.ludorival.pactjvm.mock.test.rabbitmq

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.PactConsumer
import io.github.ludorival.pactjvm.mock.clearPact
import io.github.ludorival.pactjvm.mock.getCurrentPact
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.github.ludorival.pactjvm.mock.test.NonDeterministicPact
import io.github.ludorival.pactjvm.mock.test.orderservice.OrderMessage
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mock.test.shoppingservice.config.RabbitMQConfig
import io.github.ludorival.pactjvm.mock.test.shoppingservice.messaging.ShoppingListOrderPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.LocalDate
import au.com.dius.pact.core.model.messaging.MessagePact

@PactConsumer(NonDeterministicPact::class)
class RabbitMQPactTest {

    private val rabbitTemplate = mockk<RabbitTemplate>()
    private val publisher = ShoppingListOrderPublisher(rabbitTemplate)

    @BeforeEach
    fun setUp() {
        clearPact(ORDER_SERVICE, SHOPPING_LIST)
    }

    @Test
    fun `test publisher sends shopping list as order message`() {
        val shoppingList = ShoppingList(
            id = 123L,
            title = "My Shopping List",
            userId = 456L,
            items = listOf(
                ShoppingList.Item(1L, "Apple", 2),
                ShoppingList.Item(2L, "Banana", 3)
            ),
            createdAt = LocalDate.parse("2024-01-21")
        )

        given {
            uponReceiving {
                rabbitTemplate.convertAndSend(
                    any<String>(),
                    any<String>(),
                    any<OrderMessage>()
                )
            }.withDescription {
                "Shopping list ordered message"
            }.given {
                state("shopping list ordered", mapOf(
                    "exchange" to RabbitMQConfig.EXCHANGE_NAME,
                    "routing_key" to RabbitMQConfig.ROUTING_KEY,
                    "shopping_list_id" to shoppingList.id.toString()
                ))
            }.returns(Unit)
        } `when` {
            publisher.publishOrderFromShoppingList(shoppingList)
        } then {
            with(getCurrentPact<MessagePact>(ORDER_SERVICE, SHOPPING_LIST)!!) {
                assertEquals(1, messages.size)
                with(messages.first()) {
                    assertEquals("Shopping list ordered message", description)
                    assertEquals("shopping list ordered", providerStates.first().name)
                    with(providerStates.first().params) {
                        assertEquals(RabbitMQConfig.EXCHANGE_NAME, get("exchange"))
                        assertEquals(RabbitMQConfig.ROUTING_KEY, get("routing_key"))
                        assertEquals("123", get("shopping_list_id"))
                    }
                    assertEquals(
                        """{"shopping_list_id":"123","user_id":456,"items":[{"name":"Apple","quantity":2},{"name":"Banana","quantity":3}]}""",
                        contents.valueAsString()
                    )
                }
            }
        }
    }

    companion object {
        const val ORDER_SERVICE = "order-service"
        const val SHOPPING_LIST = "shopping-list"
    }
}