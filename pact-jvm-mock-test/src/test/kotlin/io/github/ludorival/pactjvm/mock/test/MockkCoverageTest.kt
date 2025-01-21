package io.github.ludorival.pactjvm.mock.test

import au.com.dius.pact.core.model.RequestResponseInteraction
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.matchingrules.RegexMatcher
import au.com.dius.pact.core.model.matchingrules.TypeMatcher
import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.PactConsumer
import io.github.ludorival.pactjvm.mock.anError
import io.github.ludorival.pactjvm.mock.clearPact
import io.github.ludorival.pactjvm.mock.getCurrentPact
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset

@PactConsumer(NonDeterministicPact::class)
class MockkCoverageTest {

    val restTemplate = mockk<RestTemplate>()

    @BeforeEach
    fun setUp() {
        clearPact("shopping-list", API_1)
    }

    @Test
    fun `should intercept simple stub`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            } returns ResponseEntity.ok("Hello World")
        } `when`{
            restTemplate.getForEntity(TEST_API_1_URL, String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
            }
        }
    }


    @Test
    fun `should intercept with provider state and description`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            } withDescription { 
                "Get user profile" 
            } given { 
                state("user exists", mapOf("userId" to "123"))
            } returns ResponseEntity.ok("User Profile")
        } `when` {
            restTemplate.getForEntity("$TEST_API_1_URL/users/123", String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
                with(interactions.first()) {
                    assertEquals("Get user profile", description)
                    assertEquals("user exists", providerStates.first().name)
                    assertEquals("123", providerStates.first().params.get("userId"))
                }
            }
        }
    }

    @Test
    fun `should intercept with matching rules`() {
        given {
            uponReceiving {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    eq(String::class.java)
                )
            }.matchingRequest {
                header("Content-Type", RegexMatcher( "application/json.*"))
            }.macthingResponse {
                body("id", TypeMatcher)
            }.returns(ResponseEntity.ok("""{"id": "123"}"""))
        } `when` {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(
                mapOf(
                    "name" to "John",
                    "email" to "john@example.com"
                ),
                headers
            )
            restTemplate.postForEntity("$TEST_API_1_URL/users", request, String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
                with(interactions.first() as RequestResponseInteraction) {
                    assertNotNull(request.matchingRules)
                    assertNotNull(response.matchingRules)
                }
            }
        }
    }

    @Test
    fun `should handle multiple responses`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            }.returnsMany(
                ResponseEntity.ok("First response"),
                ResponseEntity.ok("Second response"),
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            )
        } `when` {
            restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java)
            restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java)
            restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java)
        } then {
            with(currentPact().interactions.filterIsInstance<RequestResponseInteraction>()) {
                assertEquals(3, size)
                assertEquals(200, this[0].response.status)
                assertEquals(200, this[1].response.status)
                assertEquals(404, this[2].response.status)
            }
        }
    }

    @Test
    fun `should handle error responses`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            }.throws(anError(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Service unavailable")))
        } `when` {
            assertThrows<HttpClientErrorException> {
                restTemplate.getForEntity("$TEST_API_1_URL/error", String::class.java)
            }
        } then {
            with(currentPact().interactions.filterIsInstance<RequestResponseInteraction>()) {
                assertEquals(1, size)
                assertEquals(500, this[0].response.status)
                assertEquals("Service unavailable", this[0].response.body.valueAsString())
            }
        }
    }

    @Test
    fun `should handle Spring Error Exceptions responses`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            }.throws(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON}, """{"error":"VALIDATION_FAILURE", "error_description": "The format is not supported"}""".toByteArray(),Charset.defaultCharset()))
        } `when` {
            assertThrows<HttpClientErrorException> {
                restTemplate.getForEntity("$TEST_API_1_URL/error", String::class.java)
            }
        } then {
            with(currentPact().interactions.filterIsInstance<RequestResponseInteraction>()) {
                assertEquals(1, size)
                assertEquals(400, this[0].response.status)
                assertTrue(this[0].response.body.contentType.isJson())
                assertEquals(""""{\"error\":\"VALIDATION_FAILURE\", \"error_description\": \"The format is not supported\"}"""", this[0].response.body.valueAsString())
            }
        }
    }

    @Test
    fun `should handle chained responses with andThen`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            }.withDescription { "Initial successful response" }
             .returns(ResponseEntity.ok("Initial response"))
             .withDescription { "Second successful response" }
             .andThen(ResponseEntity.ok("Second response"))
             .withDescription { "Error response" }
             .andThenThrows(anError(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request")))
             .withDescription { "Not found response" }
             .andThen(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
        } `when` {
            val responses = mutableListOf<ResponseEntity<String>>()
            responses.add(restTemplate.getForEntity("$TEST_API_1_URL/chain", String::class.java))
            responses.add(restTemplate.getForEntity("$TEST_API_1_URL/chain", String::class.java))

            assertThrows<HttpClientErrorException> {
                restTemplate.getForEntity("$TEST_API_1_URL/chain", String::class.java)
            }

            responses.add(restTemplate.getForEntity("$TEST_API_1_URL/chain", String::class.java))
        } then {
            with(currentPact().interactions.filterIsInstance<RequestResponseInteraction>()) {
                assertEquals(4, size)
                with(this[0]) {
                    assertEquals("Initial successful response", description)
                    assertEquals(200, response.status)
                    assertEquals("Initial response", response.body.valueAsString())
                }
                with(this[1]) {
                    assertEquals("Second successful response", description)
                    assertEquals(200, response.status)
                    assertEquals("Second response", response.body.valueAsString())
                }
                with(this[2]) {
                    assertEquals("Error response", description)
                    assertEquals(400, response.status)
                    assertEquals("Invalid request", response.body.valueAsString())
                }
                with(this[3]) {
                    assertEquals("Not found response", description)
                    assertEquals(404, response.status)
                    assertTrue(response.body.isNotPresent())
                }
            }
        }
    }

    @Test
    fun `should use interaction content to set description and provider state`() {
        given {
            uponReceiving {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    eq(String::class.java)
                )
            }.withDescription {
                "POST request to ${call.firstArg<String>()} with ${(call.secondArg<HttpEntity<Map<*, *>>>().body.size)} parameters"
            }.given {
                val httpEntity = call.secondArg<HttpEntity<*>>()
                state(
                    "request contains required fields",
                    mapOf(
                        "method" to "POST",
                        "path" to call.firstArg<String>(),
                        "contentType" to (httpEntity.headers.contentType?.toString() ?: "")
                    )
                )
            }.returns(ResponseEntity.ok("Success"))
        } `when` {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(
                mapOf(
                    "name" to "John",
                    "email" to "john@example.com"
                ),
                headers
            )
            restTemplate.postForEntity("$TEST_API_1_URL/users", request, String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
                with(interactions.first()) {
                    assertEquals("POST request to http://localhost:8080/service1/api/v1/users with 2 parameters", description)
                    assertEquals("request contains required fields", providerStates.first().name)
                    with(providerStates.first().params) {
                        assertEquals("POST", get("method")?.toString())
                        assertEquals("http://localhost:8080/service1/api/v1/users", get("path"))
                        assertEquals("application/json", get("contentType"))
                    }
                }
            }
        }
    }

    @Test
    fun `should not fail test when exception is not supported by adapter`() {
        class UnsupportedCustomException(message: String) : RuntimeException(message)

        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            }.throws(UnsupportedCustomException("Custom error"))
        } `when` {
            assertThrows<UnsupportedCustomException> {
                restTemplate.getForEntity("$TEST_API_1_URL/unsupported-error", String::class.java)
            }
        } then {
            assertNull(currentPactOrNull())
        }
    }

    companion object {
        val API_1 = "service1"
        val TEST_API_1_URL = "http://localhost:8080/$API_1/api/v1"

        private fun currentPact(): RequestResponsePact {
            return getCurrentPact<RequestResponsePact>("shopping-list", API_1) 
                ?: throw AssertionError("Expected pact to be non-null")
        }

        private fun currentPactOrNull(): RequestResponsePact? {
            return getCurrentPact<RequestResponsePact>("shopping-list", API_1)
        }
    }
}
