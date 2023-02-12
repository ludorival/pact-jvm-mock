package io.github.ludorival.pactjvm.mockk.spring

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ludorival.pactjvm.mockk.PactMockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object ShoppingPactExtension : SpringPactMockkExtension(provider = "shopping-list") {

    private val atomicInteger = AtomicInteger(0)

    override fun afterAll(context: ExtensionContext?) {
        val value = atomicInteger.incrementAndGet()
        val objectMapper = ObjectMapper()
        super.afterAll(context)
        val result = readPacts(objectMapper = objectMapper)
        assertEquals(2, result?.size)
        if (value == 2) {
            val expectations = readPacts("${PactMockk.pactDirectory}-expectation", objectMapper)
            assertEquals(expectations?.toPrettyJson(objectMapper), result?.toPrettyJson(objectMapper))
        }

    }

    private fun Any.toPrettyJson(objectMapper: ObjectMapper) =
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

    private fun readPacts(directory: String = PactMockk.pactDirectory, objectMapper: ObjectMapper) =
        File(directory).listFiles()?.associate { it.name to objectMapper.readTree(it) }
}
