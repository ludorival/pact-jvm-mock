package io.github.ludorival.pactjvm.mock.spring.providers.shoppingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

import java.time.LocalDate
@SpringBootApplication
@ComponentScan(basePackages = ["io.github.ludorival.pactjvm.mock.spring.providers.shoppingservice"])
open class ShoppingServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.runApplication<ShoppingServiceApplication>(*args)
        }
    }
}
