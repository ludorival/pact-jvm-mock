package io.github.ludorival.pactjvm.mock.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mock.PactOptions
import io.github.ludorival.pactjvm.mock.pactOptions
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate
import io.github.ludorival.pactjvm.mock.test.shoppingservice.CUSTOM_OBJECT_MAPPER
import io.github.ludorival.pactjvm.mock.serializerAsDefault
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter


object NonDeterministicPact {

    private val PACT_DIRECTORY = PactOptions.DEFAULT_OPTIONS.pactDirectory

    val CUSTOM_OBJECT_MAPPER: ObjectMapper = Jackson2ObjectMapperBuilder().serializerByType(
        LocalDate::class.java,
        serializerAsDefault<LocalDate>("2023-01-01")
    ).serializationInclusion(JsonInclude.Include.NON_NULL).build()

    val pactOptions = pactOptions {
            consumer = "shopping-list"
            pactDirectory = PACT_DIRECTORY
            objectMapperCustomizer = {
                if (it == "shopping-service") {
                    CUSTOM_OBJECT_MAPPER
                } else null }
            addAdapter(SpringRestTemplateMockkAdapter())
        }
}