package io.github.ludorival.pactjvm.mock.test.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["io.github.ludorival.pactjvm.mock.test.userservice"])
open class UserServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.runApplication<UserServiceApplication>(*args)
        }
    }
} 