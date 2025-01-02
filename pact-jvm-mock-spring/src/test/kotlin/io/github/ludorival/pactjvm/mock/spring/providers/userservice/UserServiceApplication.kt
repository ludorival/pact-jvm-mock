package io.github.ludorival.pactjvm.mock.spring.providers.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
@SpringBootApplication
@ComponentScan(basePackages = ["io.github.ludorival.pactjvm.mock.spring.providers.userservice"])
open class UserServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.runApplication<UserServiceApplication>(*args)
        }
    }
}   