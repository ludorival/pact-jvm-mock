package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.EnablePactMock
import io.github.ludorival.pactjvm.mock.clearPact
import io.github.ludorival.pactjvm.mock.getCurrentPact
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import au.com.dius.pact.core.model.RequestResponsePact
import org.junit.jupiter.api.Assertions.assertEquals

@EnablePactMock(DeterministicPact::class)
class DeterministicPactTest {

    private val restTemplate = mockk<RestTemplate>()
    private val TEST_URL = "http://localhost:8080/provider/test"
    private val CONSUMER = "shopping-list"
    private val PROVIDER = "provider"

    @BeforeEach
    fun setUp() {
        clearPact(CONSUMER, PROVIDER)
    }

    @Test
    fun `should throw error when recording same interaction twice`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(TEST_URL, String::class.java)
            } withDescription {
                "should throw error when recording same interaction twice"
            } returns ResponseEntity.ok("Hello World") andThen ResponseEntity.ok("Different response")
        } `when` {
            // First call succeeds with "Hello World"
            restTemplate.getForEntity(TEST_URL, String::class.java)

            // Second call with different response should throw
            assertThrows<IllegalStateException> {
                restTemplate.getForEntity(TEST_URL, String::class.java)
            }
        } then { it: IllegalStateException ->
            assertEquals("""The interaction with description "should throw error when recording same interaction twice" has changed
The changes are:

-Captured
+Received

 {
  "description": "should throw error when recording same interaction twice",
  "request": {
    "method": "GET",
    "path": "/provider/test"
  },
  "response": {
    "body": "
- Hello World
+ Different response
 ",
    "status": 200
  }
}

The Pact contract should be deterministic.
See https://github.com/ludorival/pact-jvm-mock?tab=readme-ov-file#make-your-contract-deterministic for more details.
====================================================""".trim(),
                    it.message!!.replace("\\x1B\\[[0-9;]*[a-zA-Z]".toRegex(), "").trim())
            val pact = getCurrentPact<RequestResponsePact>(CONSUMER, PROVIDER)
            assert(pact != null) { "Pact should have been created" }
            assert(pact!!.interactions.size == 1) { "Should have recorded exactly one interaction" }
        }
    }
}