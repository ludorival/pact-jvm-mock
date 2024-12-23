package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.pactjvm.mockk.PactOptions
import io.github.ludorival.pactjvm.mockk.pactOptions
import io.github.ludorival.pactjvm.mockk.writePacts
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import io.github.ludorival.pactjvm.mockk.spring.providers.shoppingservice.CUSTOM_OBJECT_MAPPER
import io.github.ludorival.pactjvm.mockk.serializerAsDefault
import java.time.LocalDate
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.ObjectMapper


object DeterministicPact : BeforeAllCallback, AfterAllCallback {

    private val PACT_DIRECTORY = "${PactOptions.DEFAULT_OPTIONS.pactDirectory}-deterministic"

    private val CUSTOM_OBJECT_MAPPER : ObjectMapper = Jackson2ObjectMapperBuilder()
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .serializerByType(
        LocalDate::class.java,
        serializerAsDefault<LocalDate>("2023-01-01")
    ).build()

    override fun beforeAll(context: ExtensionContext?) {
        pactOptions {
            consumer = "shopping-list"
            pactDirectory = PACT_DIRECTORY
            isDeterministic = true
            objectMapperCustomizer = { if (it == "shopping-service") CUSTOM_OBJECT_MAPPER else null }
            addAdapter(SpringRestTemplateMockkAdapter())
        }
    }

    override fun afterAll(context: ExtensionContext?) = writePacts()

//    private fun Any.toPrettyJson(objectMapper: ObjectMapper) =
//        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
//
//    private fun readPacts(directory: String = PACT_DIRECTORY, objectMapper: ObjectMapper) =
//        File(directory).listFiles()?.associate { it.name to objectMapper.readTree(it) }
}
