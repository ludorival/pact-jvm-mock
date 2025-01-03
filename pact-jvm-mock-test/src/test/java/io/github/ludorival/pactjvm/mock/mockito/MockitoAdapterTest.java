package io.github.ludorival.pactjvm.mock.mockito;

import io.github.ludorival.pactjvm.mock.PactConsumer;
import io.github.ludorival.pactjvm.mock.PactOptions;
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter;
import io.github.ludorival.pactjvm.mock.test.userservice.UserPreferences;
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

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

    private static final UserProfile USER_PROFILE = new UserProfile(
            1L,
            "John Doe",
            "john.doe@example.com",
            new UserPreferences(123L)
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testWillRespond() {
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(UserProfile.class)))
            .withDescription("Get user profile")
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        ResponseEntity<UserProfile> response = restTemplate.getForEntity(
                "http://user-service/users/1",
                UserProfile.class
        );
    }

    @Test
    void testWillRespondWithDescription() {
        PactMockito.uponReceiving(restTemplate.getForEntity(any(String.class), eq(UserProfile.class)))
            .withDescription("Get user profile")
            .withProviderState("User exists", Map.of("userId", "1"))
            .thenReturn(ResponseEntity.ok(USER_PROFILE));

        ResponseEntity<UserProfile> response = restTemplate.getForEntity(
                "http://user-service/users/1",
                UserProfile.class
        );
    }

    @Test
    void testWillRespondWithError() {
        UserProfile invalidProfile = new UserProfile(
                0L,
                "",
                "invalid-email",
                new UserPreferences(456L)
        );

        String errorMessage = "Invalid user data: name cannot be empty and email must be valid";
        PactMockito.uponReceiving(restTemplate.postForEntity(any(String.class), any(UserProfile.class), eq(UserProfile.class)))
            .withDescription("Create user profile with invalid data")
            .thenThrow(HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    errorMessage,
                    new HttpHeaders(),
                    errorMessage.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

        assertThrows(HttpClientErrorException.BadRequest.class, () -> {
            restTemplate.postForEntity("/api/users", invalidProfile, UserProfile.class);
        });
    }
} 