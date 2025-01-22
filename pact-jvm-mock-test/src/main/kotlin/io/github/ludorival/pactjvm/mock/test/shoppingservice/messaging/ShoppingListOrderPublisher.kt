package io.github.ludorival.pactjvm.mock.test.shoppingservice.messaging

import io.github.ludorival.pactjvm.mock.test.orderservice.OrderMessage
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mock.test.shoppingservice.config.RabbitMQConfig
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class ShoppingListOrderPublisher(private val rabbitTemplate: RabbitTemplate) {
    
    fun publishOrderFromShoppingList(shoppingList: ShoppingList) {
        val orderMessage = OrderMessage.fromShoppingList(shoppingList)
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            orderMessage
        )
    }
} 