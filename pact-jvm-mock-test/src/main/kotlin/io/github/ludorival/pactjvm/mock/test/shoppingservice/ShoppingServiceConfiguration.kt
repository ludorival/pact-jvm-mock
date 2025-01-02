package io.github.ludorival.pactjvm.mock.test.shoppingservice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
open class ShoppingServiceConfiguration {

    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper = CUSTOM_OBJECT_MAPPER
}

val CUSTOM_OBJECT_MAPPER: ObjectMapper = Jackson2ObjectMapperBuilder()
    .modules(JavaTimeModule())
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .serializationInclusion(JsonInclude.Include.NON_NULL)
    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build() 