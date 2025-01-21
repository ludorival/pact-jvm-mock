package io.github.ludorival.pactjvm.mock.test.orderservice

import io.github.ludorival.pactjvm.mock.test.shoppingservice.config.RabbitMQConfig
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
open class OrderConsumerService {

    @RabbitListener(queues = [RabbitMQConfig.ROUTING_KEY])
    fun handleShoppingListOrdered(orderMessage: OrderMessage) {
        // Process the order message
        println("Received order message: $orderMessage")
    }
} 