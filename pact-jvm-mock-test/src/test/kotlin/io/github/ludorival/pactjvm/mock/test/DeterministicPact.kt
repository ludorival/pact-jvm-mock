package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.pactjvm.mock.PactOptions
import io.github.ludorival.pactjvm.mock.pactOptions
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import io.github.ludorival.pactjvm.mock.test.NonDeterministicPact.CUSTOM_OBJECT_MAPPER
import io.github.ludorival.pactjvm.mock.serializerAsDefault
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter
import java.time.LocalDate
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.ObjectMapper


object DeterministicPact  {

    private val PACT_DIRECTORY = "${PactOptions.DEFAULT_OPTIONS.pactDirectory}-deterministic"

    val pactOptions = pactOptions {
            consumer = "shopping-list"
            pactDirectory = PACT_DIRECTORY
            isDeterministic = true
            objectMapperCustomizer = { if (it == "shopping-service") CUSTOM_OBJECT_MAPPER else null }
            addAdapter(SpringRestTemplateMockkAdapter())
        }
}
