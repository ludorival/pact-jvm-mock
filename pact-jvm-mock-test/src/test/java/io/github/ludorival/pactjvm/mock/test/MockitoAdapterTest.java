package io.github.ludorival.pactjvm.mock.test;

import io.github.ludorival.pactjvm.mock.mockito.PactMockito;
import io.github.ludorival.pactjvm.mock.PactConsumer;
import io.github.ludorival.pactjvm.mock.PactOptions;
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter;
import io.github.ludorival.pactjvm.mock.test.userservice.UserPreferences;
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile;
import io.github.ludorival.pactjvm.mock.test.consumer.userservice.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@PactConsumer(MockitoAdapterTest.TestPactOptions.class)
public class MockitoAdapterTest {

    public static class TestPactOptions {
        public static final PactOptions pactOptions;

        static {
            PactOptions.Builder builder = new PactOptions.Builder();
            builder.setConsumer("mockito-test-consumer");
            builder.addAdapter(new SpringRestTemplateMockkAdapter());
            pactOptions = builder.build();
        }
    }

    @Mock
    private RestTemplate restTemplate;

    private UserServiceClient userServiceClient;

    private static final UserProfile USER_PROFILE = new UserProfile(
            1L,
            "John Doe",
            "john.doe@example.com",
            new UserPreferences(123L)
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userServiceClient = new UserServiceClient(restTemplate);
    }

    @Test
    void testGetUserProfile() {
        PactMockito.uponReceiving(restTemplate.getForEntity(
                any(String.class),
                eq(UserProfile.class),
                any(Long.class)))
            .withDescription("Get user profile")
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        UserProfile response = userServiceClient.getUserProfile(1L);
        assertEquals(USER_PROFILE, response);
    }

    @Test
    void testGetUserProfileWithProviderState() {
        PactMockito.uponReceiving(restTemplate.getForEntity(
                any(String.class),
                eq(UserProfile.class),
                any(Long.class)))
            .withDescription("Get user profile")
            .withProviderState("User exists", Map.of("userId", "1"))
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        UserProfile response = userServiceClient.getUserProfile(1L);
        assertEquals(USER_PROFILE, response);
    }

    @Test
    void testSetPreferredShoppingListError() {
        String errorMessage = "Invalid shopping list preference";
        PactMockito.uponReceiving(restTemplate.exchange(
                any(RequestEntity.class),
                eq(UserProfile.class)))
            .withDescription("Update user preferences with invalid shopping list")
            .withProviderState("Invalid shopping list", Map.of("userId", "1", "shoppingListId", "456"))
            .thenThrow(HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    errorMessage,
                    new HttpHeaders(),
                    errorMessage.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            userServiceClient.setPreferredShoppingList(1L, 456L);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(errorMessage, exception.getResponseBodyAsString());
    }
} 