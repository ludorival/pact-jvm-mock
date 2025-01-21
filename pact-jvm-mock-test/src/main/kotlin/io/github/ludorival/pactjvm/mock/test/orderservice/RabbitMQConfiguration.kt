package io.github.ludorival.pactjvm.mock.test.orderservice

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
import io.github.ludorival.pactjvm.mock.test.shoppingservice.config.RabbitMQConfig

@Configuration
class RabbitMQConfiguration {

    @Bean
    fun queue(): Queue = Queue(RabbitMQConfig.ROUTING_KEY)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(RabbitMQConfig.EXCHANGE_NAME)

    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(RabbitMQConfig.ROUTING_KEY)

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
} 