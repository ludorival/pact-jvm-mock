package io.github.ludorival.pactjvm.mock.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mock.spring.serializerAsDefault
import io.github.ludorival.pactjvm.mock.test.shoppingservice.objectMapperBuilder
import java.time.LocalDate

object ObjectMapperConfig {

    fun by(providerName: String): ObjectMapper? = if (providerName == "shopping-service") objectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializerByType(
            LocalDate::class.java,
            serializerAsDefault<LocalDate>("2023-01-01")
        ).serializationInclusion(JsonInclude.Include.NON_NULL).build() else null
}
