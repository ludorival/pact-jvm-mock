package io.github.ludorival.pactjvm.mock.test.shoppingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["io.github.ludorival.pactjvm.mock.test.shoppingservice"])
open class ShoppingServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.runApplication<ShoppingServiceApplication>(*args)
        }
    }
} 