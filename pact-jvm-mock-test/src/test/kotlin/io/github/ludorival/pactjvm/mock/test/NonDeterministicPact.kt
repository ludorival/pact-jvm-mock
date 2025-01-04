package io.github.ludorival.pactjvm.mock.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.github.ludorival.pactjvm.mock.PactConfiguration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate
import io.github.ludorival.pactjvm.mock.test.shoppingservice.objectMapperBuilder
import io.github.ludorival.pactjvm.mock.serializerAsDefault
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter

object NonDeterministicPact : PactConfiguration("shopping-list", SpringRestTemplateMockkAdapter()) {

    private val CUSTOM_OBJECT_MAPPER: ObjectMapper = objectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializerByType(
            LocalDate::class.java,
            serializerAsDefault<LocalDate>("2023-01-01")
        ).serializationInclusion(JsonInclude.Include.NON_NULL).build()

    override fun customizeObjectMapper(providerName: String): ObjectMapper? = 
        if (providerName == "shopping-service") CUSTOM_OBJECT_MAPPER else null

}
