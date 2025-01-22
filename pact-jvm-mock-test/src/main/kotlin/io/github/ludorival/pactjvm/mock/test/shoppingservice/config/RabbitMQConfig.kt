package io.github.ludorival.pactjvm.mock.test.shoppingservice.config

import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitMQConfig {
    
    companion object {
        const val EXCHANGE_NAME = "shopping.topic"
        const val ROUTING_KEY = "shopping.list.ordered"
    }

    @Bean
    open fun exchange(): TopicExchange = TopicExchange(EXCHANGE_NAME)

    @Bean
    open fun messageConverter() = Jackson2JsonMessageConverter()

    @Bean
    open fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: Jackson2JsonMessageConverter): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }
} 