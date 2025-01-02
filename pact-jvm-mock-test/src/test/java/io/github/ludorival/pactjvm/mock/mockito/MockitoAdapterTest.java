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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static io.github.ludorival.pactjvm.mock.mockito.MockitoUtils.willRespond;
import static io.github.ludorival.pactjvm.mock.mockito.MockitoUtils.willRespondWith;
import static io.github.ludorival.pactjvm.mock.mockito.MockitoUtils.willRespondWithError;
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
        willRespond(
            when(restTemplate.getForEntity(any(String.class), eq(UserProfile.class))),
            ResponseEntity.ok(USER_PROFILE)
        );

        ResponseEntity<UserProfile> response = restTemplate.getForEntity(
                "http://user-service/users/1",
                UserProfile.class
        );
    }

    @Test
    void testWillRespondWithDescription() {
        willRespondWith(
            when(restTemplate.getForEntity(any(String.class), eq(UserProfile.class))),
            scope -> {
                scope.description("Get user profile");
                scope.providerState("User exists", Map.of("userId", "1"));
                return ResponseEntity.ok(USER_PROFILE);
            }
        );

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

        willRespondWithError(
            when(restTemplate.postForEntity(any(URI.class), any(), eq(UserProfile.class))),
            scope -> {
                scope.description("Create user profile - validation error");
                scope.providerState("Invalid user data", Collections.emptyMap());
                return ResponseEntity.badRequest()
                        .body("Invalid user data: name cannot be empty and email must be valid");
            }
        );

        assertThrows(HttpClientErrorException.BadRequest.class, () -> 
            restTemplate.postForEntity(
                URI.create("http://user-service/users"),
                invalidProfile,
                UserProfile.class
            )
        );
    }
} 