package io.github.ludorival.pactjvm.mock.test;

import io.github.ludorival.pactjvm.mock.mockito.PactMockito;
import io.github.ludorival.pactjvm.mock.EnablePactMock;
import io.github.ludorival.pactjvm.mock.PactConfiguration;
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter;
import io.github.ludorival.pactjvm.mock.test.userservice.UserPreferences;
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile;

import io.github.ludorival.pactjvm.mock.test.consumer.userservice.UserServiceClient;
import io.github.ludorival.pactjvm.mock.test.consumer.shoppingservice.ShoppingServiceClient;
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


@EnablePactMock(MockitoAdapterTest.TestPactConfig.class)
public class MockitoAdapterTest {

    public static class TestPactConfig extends PactConfiguration {
        public TestPactConfig() {
            super(new SpringRestTemplateMockAdapter("mockito-test-consumer"));
        }

    }

    @Mock
    private RestTemplate restTemplate;

    private UserServiceClient userServiceClient;

    private ShoppingServiceClient shoppingServiceClient;

    private static final UserProfile USER_PROFILE = new UserProfile(
            123L,
             "User name", "user@email.com", new UserPreferences(
        1L
    )
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userServiceClient = new UserServiceClient(restTemplate);
        shoppingServiceClient = new ShoppingServiceClient(restTemplate);
    }

    @Test
    void testGetUserProfile() {
        PactMockito.uponReceiving(restTemplate.getForEntity(
                any(String.class),
                eq(UserProfile.class),
                any(Long.class)))
            .withDescription("Get user profile")
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        UserProfile response = userServiceClient.getUserProfile(USER_PROFILE.getId());
        assertEquals(USER_PROFILE, response);
    }

    @Test
    void testGetUserProfilegiven() {
        PactMockito.uponReceiving(restTemplate.getForEntity(
                any(String.class),
                eq(UserProfile.class),
                any(Long.class)))
            .withDescription("Get user profile")
            .given((builder) -> builder.state("The user has a preferred shopping list", Map.of("userId", USER_PROFILE.getId())))
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        UserProfile response = userServiceClient.getUserProfile(USER_PROFILE.getId());
        assertEquals(USER_PROFILE, response);
    }

    @Test
    void testSetPreferredShoppingListError() {
        String errorMessage = "The title contains unexpected character";
        PactMockito.uponReceiving(restTemplate.postForEntity(
                any(URI.class),
                any(Map.class),
                eq(ShoppingList.class)))
            .withDescription("should return a 400 Bad request")
            .given((builder) -> builder.state("The request should return a 400 Bad request", Collections.emptyMap()))
            .thenThrow(HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    errorMessage,
                    new HttpHeaders(),
                    errorMessage.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            shoppingServiceClient.createShoppingList(123L, "Unexpected character \\s");
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(errorMessage, exception.getResponseBodyAsString());
    }
} 