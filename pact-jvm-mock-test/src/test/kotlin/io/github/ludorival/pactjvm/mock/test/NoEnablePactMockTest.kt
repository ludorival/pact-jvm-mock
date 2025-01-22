package io.github.ludorival.pactjvm.mock.test

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mock.*
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import au.com.dius.pact.core.model.RequestResponsePact

class NoEnablePactMockTest {

    val restTemplate = mockk<RestTemplate>()

    @BeforeEach
    fun setUp() {
        clearPact("shopping-list", API_1)
    }

    @Test
    fun `should not intercept when PactConsumer annotation is missing`() {
        given {
            uponReceiving {
                restTemplate.getForEntity(any<String>(), eq(String::class.java))
            } returns ResponseEntity.ok("Hello World")
        } `when` {
            restTemplate.getForEntity(TEST_API_1_URL, String::class.java)
        } then {
            // Verify no interactions were recorded since @PactConsumer is missing
            val pact = getCurrentPact<RequestResponsePact>("shopping-list", API_1)
            assertTrue(pact == null || pact.interactions.isEmpty(), "Expected no interactions to be recorded")
        }
    }

    companion object {
        val API_1 = "service1"
        val TEST_API_1_URL = "http://localhost:8080/$API_1/api/v1"
    }
} 