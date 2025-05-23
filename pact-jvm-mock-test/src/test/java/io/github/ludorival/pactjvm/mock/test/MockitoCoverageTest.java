package io.github.ludorival.pactjvm.mock.test;

import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import au.com.dius.pact.core.model.matchingrules.RegexMatcher;
import au.com.dius.pact.core.model.matchingrules.TypeMatcher;
import io.github.ludorival.pactjvm.mock.*;
import io.github.ludorival.pactjvm.mock.mockito.PactMockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.ludorival.pactjvm.mock.UtilsKt.anError;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@EnablePactMock(NonDeterministicPact.class)
public class MockitoCoverageTest {

    private static final String API_1 = "service1";
    private static final String TEST_API_1_URL = "http://localhost:8080/" + API_1 + "/api/v1";

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UtilsKt.clearPact("shopping-webapp", API_1);
    }

    @Test
    void shouldInterceptSimpleStub() {
        // given
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Hello World"));

        // when
        restTemplate.getForEntity(TEST_API_1_URL, String.class);

        // then
        Pact pact = currentPact();
        assertNotNull(pact);
        assertEquals(1, pact.getInteractions().size());
    }

    @Test
    void shouldInterceptgivenAndDescription() {
        // given
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .withDescription((interaction) -> "Get user profile")
                .given((builder) -> builder.state("user exists", Map.of("userId", "123")))
                .thenReturn(ResponseEntity.ok("User Profile"));

        // when
        restTemplate.getForEntity(TEST_API_1_URL + "/users/123", String.class);

        // then
        Pact pact = currentPact();
        assertNotNull(pact);
        assertEquals(1, pact.getInteractions().size());
        RequestResponseInteraction interaction = (RequestResponseInteraction) pact.getInteractions().get(0);
        assertEquals("Get user profile", interaction.getDescription());
        assertNotNull(interaction.getProviderStates());
        assertEquals("user exists", interaction.getProviderStates().get(0).getName());
        assertEquals("123", interaction.getProviderStates().get(0).getParams().get("userId"));
    }

    @Test
    void shouldInterceptmatching() {
        // given
        PactMockito.uponReceiving(restTemplate.postForEntity(
                any(String.class),
                any(HttpEntity.class),
                eq(String.class)
        ))
        .matchingRequest(builder -> builder.header("Content-Type", new RegexMatcher("application/json.*")))
        .matchingResponse(builder -> builder.body("id", TypeMatcher.INSTANCE))
        .thenReturn(ResponseEntity.ok("{\"id\": \"123\"}"));

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(
                Map.of("name", "John", "email", "john@example.com"),
                headers
        );
        restTemplate.postForEntity(TEST_API_1_URL + "/users", request, String.class);

        // then
        Pact pact = currentPact();
        assertNotNull(pact);
        assertEquals(1, pact.getInteractions().size());
        RequestResponseInteraction interaction = (RequestResponseInteraction) pact.getInteractions().get(0);
        assertNotNull(interaction.getRequest().getMatchingRules());
        assertNotNull(interaction.getResponse().getMatchingRules());
    }

    @Test
    void shouldHandleMultipleResponses() {
        // given
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .withDescription("First response")
                .andThenReturn(ResponseEntity.ok("First response"))
                .withDescription("Second response")
                .andThenReturn(ResponseEntity.ok("Second response"))
                .withDescription("Not found response")
                .andThenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        // when
        restTemplate.getForEntity(TEST_API_1_URL + "/data", String.class);
        restTemplate.getForEntity(TEST_API_1_URL + "/data", String.class);
        restTemplate.getForEntity(TEST_API_1_URL + "/data", String.class);

        // then


        // Verify pact interactions
        Pact pact = currentPact();
        assertNotNull(pact);
        assertEquals(3, pact.getInteractions().size());
        
        // First interaction
        RequestResponseInteraction interaction = (RequestResponseInteraction) pact.getInteractions().get(0);
        assertEquals("First response", interaction.getDescription());
        assertEquals(200, interaction.getResponse().getStatus());
        assertEquals("First response", interaction.getResponse().getBody().valueAsString());
        
        // Second interaction
        RequestResponseInteraction interaction1 = (RequestResponseInteraction) pact.getInteractions().get(1);
        assertEquals("Second response", interaction1.getDescription());
        assertEquals(200, interaction1.getResponse().getStatus());
        assertEquals("Second response", interaction1.getResponse().getBody().valueAsString());
        
        // Third interaction
        RequestResponseInteraction interaction2 = (RequestResponseInteraction) pact.getInteractions().get(2);
        assertEquals("Not found response", interaction2.getDescription());
        assertEquals(404, interaction2.getResponse().getStatus());
        assertTrue(interaction2.getResponse().getBody().isNotPresent());
    }

    @Test
    void shouldHandleErrorResponses() {
        // given
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .thenThrow(anError(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Service unavailable")));

        // when
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.getForEntity(TEST_API_1_URL + "/error", String.class)
        );

        // then
        Pact pact = currentPact();
        assertNotNull(pact);
        assertEquals(1, pact.getInteractions().size());
        RequestResponseInteraction interaction = (RequestResponseInteraction) pact.getInteractions().get(0);
        assertEquals(500, interaction.getResponse().getStatus());
        assertEquals("Service unavailable", interaction.getResponse().getBody().valueAsString());
    }

    @Test
    void shouldHandleChainedResponsesWithThen() {
        // given
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .withDescription("Initial successful response")
                .andThenReturn(ResponseEntity.ok("Initial response"))
                .withDescription("Second successful response")
                .andThenReturn(ResponseEntity.ok("Second response"))
                .withDescription("Error response")
                .andThenThrow(anError(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request")))
                .withDescription("Not found response")
                .andThenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        // when
        List<ResponseEntity<String>> responses = new ArrayList<>();
        responses.add(restTemplate.getForEntity(TEST_API_1_URL + "/chain", String.class));
        responses.add(restTemplate.getForEntity(TEST_API_1_URL + "/chain", String.class));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.getForEntity(TEST_API_1_URL + "/chain", String.class)
        );

        responses.add(restTemplate.getForEntity(TEST_API_1_URL + "/chain", String.class));

        // then
        Pact pact = currentPact();
        assertNotNull(pact);
        List<RequestResponseInteraction> interactions = pact.getInteractions().stream().map(interaction -> (RequestResponseInteraction)interaction).toList();
        assertEquals(4, interactions.size());

        // First interaction
        assertEquals("Initial successful response", interactions.get(0).getDescription());
        assertEquals(200, interactions.get(0).getResponse().getStatus());
        assertEquals("Initial response", interactions.get(0).getResponse().getBody().valueAsString());

        // Second interaction
        assertEquals("Second successful response", interactions.get(1).getDescription());
        assertEquals(200, interactions.get(1).getResponse().getStatus());
        assertEquals("Second response", interactions.get(1).getResponse().getBody().valueAsString());

        // Third interaction
        assertEquals("Error response", interactions.get(2).getDescription());
        assertEquals(400, interactions.get(2).getResponse().getStatus());
        assertEquals("Invalid request", interactions.get(2).getResponse().getBody().valueAsString());

        // Fourth interaction
        assertEquals("Not found response", interactions.get(3).getDescription());
        assertEquals(404, interactions.get(3).getResponse().getStatus());
        assertTrue(interactions.get(3).getResponse().getBody().isNotPresent());
    }

    @SuppressWarnings("unchecked")
    private static RequestResponsePact currentPact() {
        return UtilsKt.<RequestResponsePact>getCurrentPact("shopping-webapp", API_1);
    }
} 
