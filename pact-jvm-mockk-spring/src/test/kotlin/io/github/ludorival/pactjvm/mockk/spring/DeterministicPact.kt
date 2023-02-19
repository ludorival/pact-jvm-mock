package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.pactjvm.mockk.PactOptions
import io.github.ludorival.pactjvm.mockk.pactOptions
import io.github.ludorival.pactjvm.mockk.spring.NonDeterministicPact.CUSTOM_OBJECT_MAPPER
import io.github.ludorival.pactjvm.mockk.writePacts
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

object DeterministicPact : BeforeAllCallback, AfterAllCallback {

    private val PACT_DIRECTORY = "${PactOptions.DEFAULT_OPTIONS.pactDirectory}-deterministic"

    override fun beforeAll(context: ExtensionContext?) {
        pactOptions {
            consumer = "shopping-list"
            pactDirectory = PACT_DIRECTORY
            isDeterministic = true
            objectMapperCustomizer = { CUSTOM_OBJECT_MAPPER }
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
