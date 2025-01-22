package io.github.ludorival.pactjvm.mock.test.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["io.github.ludorival.pactjvm.mock.test.orderservice"])
open class OrderServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.runApplication<OrderServiceApplication>(*args)
        }
    }
} 