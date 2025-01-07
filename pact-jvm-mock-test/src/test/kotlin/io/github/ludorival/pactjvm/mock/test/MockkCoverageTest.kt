package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.*
import io.github.ludorival.pactjvm.mock.Matcher
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@PactConsumer(NonDeterministicPact::class)
class MockkCoverageTest {

    val restTemplate = mockk<RestTemplate>()

    @BeforeEach
    fun setUp() {
        clearPact(API_1)
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
            with(getCurrentPact(API_1)!!) {
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
            with(getCurrentPact(API_1)!!) {
                assertEquals(1, interactions.size)
                with(interactions.first()) {
                    assertEquals("Get user profile", description)
                    assertEquals("user exists", providerStates?.first()?.name)
                    assertEquals("123", providerStates?.first()?.params?.get("userId"))
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
                header("Content-Type", Matcher(match = Matcher.MatchEnum.REGEX, regex = "application/json.*"))
            }.macthingResponse {
                body("id", Matcher(match = Matcher.MatchEnum.TYPE))
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
            with(getCurrentPact(API_1)!!) {
                assertEquals(1, interactions.size)
                with(interactions.first()) {
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
            val responses = listOf(
                restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java),
                restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java),
                restTemplate.getForEntity("$TEST_API_1_URL/data", String::class.java)
            )
        } then {
            with(getCurrentPact(API_1)!!) {
                assertEquals(3, interactions.size)
                assertEquals(200, interactions[0].response.status)
                assertEquals(200, interactions[1].response.status)
                assertEquals(404, interactions[2].response.status)
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
            with(getCurrentPact(API_1)!!) {
                assertEquals(1, interactions.size)
                assertEquals(500, interactions[0].response.status)
                assertEquals("\"Service unavailable\"", interactions[0].response.body.toString())
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
            with(getCurrentPact(API_1)!!) {
                assertEquals(4, interactions.size)
                with(interactions[0]) {
                    assertEquals("Initial successful response", description)
                    assertEquals(200, response.status)
                    assertEquals("\"Initial response\"", response.body.toString())
                }
                with(interactions[1]) {
                    assertEquals("Second successful response", description)
                    assertEquals(200, response.status)
                    assertEquals("\"Second response\"", response.body.toString())
                }
                with(interactions[2]) {
                    assertEquals("Error response", description)
                    assertEquals(400, response.status)
                    assertEquals("\"Invalid request\"", response.body.toString())
                }
                with(interactions[3]) {
                    assertEquals("Not found response", description)
                    assertEquals(404, response.status)
                    assertNull(response.body)
                }
            }
        }
    }

    companion object {
        val API_1 = "service1"
        val TEST_API_1_URL = "http://localhost:8080/$API_1/api/v1"
    }
}
