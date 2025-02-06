package io.github.ludorival.pactjvm.mock.test

import au.com.dius.pact.core.model.RequestResponseInteraction
import au.com.dius.pact.core.model.RequestResponsePact
import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.EnablePactMock
import io.github.ludorival.pactjvm.mock.clearPact
import io.github.ludorival.pactjvm.mock.getCurrentPact
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@EnablePactMock(NonDeterministicPact::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
class DuplicateInteractionTest {

    private val restTemplate = mockk<RestTemplate>()
    private val testPayload = mapOf(
        "name" to "John Doe",
        "email" to "john@example.com"
    )

    @BeforeAll
    fun setUp() {
        clearPact(CONSUMER, API_1)
    }

    @Test
    @Order(1)
    fun `first test case with user creation interaction`() {
        given {
            uponReceiving {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    eq(String::class.java)
                )
            }.returns(ResponseEntity.ok("""{"id": "123", "status": "created"}"""))
        } `when` {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(testPayload, headers)
            restTemplate.postForEntity("$TEST_API_1_URL/users", request, String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
                with(interactions.first() as RequestResponseInteraction) {
                    assertEquals("first test case with user creation interaction", description)
                    assertEquals(200, response.status)
                }
            }
        }
    }

    @Test
    @Order(2)
    fun `second test case with same user creation interaction`() {
        given {
            uponReceiving {
                restTemplate.postForEntity(
                    any<String>(),
                    any<HttpEntity<Map<String, String>>>(),
                    eq(String::class.java)
                )
            }.returns(ResponseEntity.ok("""{"id": "123", "status": "created"}"""))
        } `when` {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(testPayload, headers)
            restTemplate.postForEntity("$TEST_API_1_URL/users", request, String::class.java)
        } then {
            with(currentPact()) {
                assertEquals(1, interactions.size)
                with(interactions.first() as RequestResponseInteraction) {
                    assertEquals("first test case with user creation interaction", description)
                    assertEquals(200, response.status)
                }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        clearPact(CONSUMER, API_1)
    }

    companion object {
        private const val CONSUMER = "shopping-webapp"
        private const val API_1 = "provider-service-2"
        private const val TEST_API_1_URL = "http://localhost:8080/$API_1/api/v1"

        private fun currentPact(): RequestResponsePact {
            return getCurrentPact<RequestResponsePact>(CONSUMER, API_1)
                ?: throw AssertionError("Expected pact to be non-null")
        }
    }
} 