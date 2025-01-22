Pact JVM Mock
=========================
![Build status](https://github.com/ludorival/pact-jvm-mock/actions/workflows/main.yaml/badge.svg)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/ludorival/pact-jvm-mock)
> A Kotlin library that leverages existing mocks to create Pact contract files, using the popular mocking libraries [Mockk](https://github.com/mockk/mockk) and [Mockito](https://github.com/mockito/mockito).

## Motivation

Pact is a powerful tool for ensuring the compatibility of microservices. It allows you to define contracts between your
services and test them in isolation.

However, writing these contracts can be time-consuming and repetitive. This is where pact-jvm-mock comes in. It
automatically generates Pact contracts from your existing [Mockk](https://github.com/mockk/mockk) mocks or [Mockito](https://github.com/mockito/mockito) mocks, saving you time
and reducing the risk of human error.

## Features

- Automatically generate Pact contracts from existing [Mockk](https://github.com/mockk/mockk) mocks or [Mockito](https://github.com/mockito/mockito) mocks
- Supports all common mock interactions, such as method calls and property accesses
- Compatible with Spring Rest Template client and Spring RabbitMQ messaging
- Easy to integrate with your existing testing workflow

## Getting Started in 5 minutes

### Setup

To get started with pact-jvm-mock, you'll need to add the library to your project. You can do this by adding the
following dependency to your build.gradle file:

**Gradle**

```groovy
testImplementation "io.github.ludorival:pact-jvm-mockk-spring:$pactJvmMockVersion"
```

Or if you are using Maven:

**Maven**

```xml
<!-- For intercepting Mockk calls -->
<dependency>
    <groupId>io.github.ludorival</groupId>
    <artifactId>pact-jvm-mock-mockk</artifactId>
    <version>${pactJvmMockVersion}</version>
    <scope>test</scope>
</dependency>

<!-- For intercepting Mockito calls -->
<dependency>
    <groupId>io.github.ludorival</groupId>
    <artifactId>pact-jvm-mock-mockito</artifactId>
    <version>${pactJvmMockVersion}</version>
    <scope>test</scope>
</dependency>

<!-- For intercepting Spring RestTemplate calls -->
<dependency>
    <groupId>io.github.ludorival</groupId>
    <artifactId>pact-jvm-mock-spring</artifactId>
    <version>${pactJvmMockVersion}</version>
    <scope>test</scope>
</dependency>
```

### Configure the pacts

Next, you'll need to configure the library to use your existing [Mockk](https://github.com/mockk/mockk) mocks.

For example, let's say you want to leverage existing mock of Spring RestTemplate.
Create a Kotlin object (or use an existing one) implementing `PactConfiguration`. Here is a minimal example:

```kotlin
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter

object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockAdapter())
```

For RabbitMQ messaging, you can use the `SpringRabbitMQMockAdapter`:

```kotlin
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRabbitMQMockAdapter
import com.fasterxml.jackson.databind.ObjectMapper

object MyServicePactConfig : PactConfiguration(
    SpringRabbitMQMockAdapter("my-service", customObjectMapper) // ObjectMapper is optional
)
```

### Extend your tests files

Then, to start writing contract,
you have to extend your test classes where you need to record the interactions with your providers. Like that

```kotlin
import io.github.ludorival.pactjvm.mock.mockk.EnablePactMock

@EnablePactMock(MyServicePactConfig::class)
class ShoppingServiceClientTest 
```

### Migrate lightly your existing mocks

Finally, you can use the library to generate your Pact contracts.

#### Using with Mockk (Kotlin)

The library is fully compatible with your existing Mockk code. You just need to replace all your `every` calls with `uponReceiving`:

```kotlin
// Your existing Mockk code
every {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} returns ResponseEntity.ok(USER_PROFILE)

// Simply becomes
uponReceiving {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} returns ResponseEntity.ok(USER_PROFILE)

// For RabbitMQ messaging:
uponReceiving {
    rabbitTemplate.convertAndSend(
        any<String>(), // exchange
        any<String>(), // routing key
        any<OrderMessage>() // message
    )
}.withDescription {
    "Order message sent"
}.given {
    state("order created", mapOf(
        "exchange" to "orders",
        "routing_key" to "order.created"
    ))
}.returns(Unit)
```

All your existing Mockk matchers and features continue to work exactly the same way. The library supports:
- Static responses with `returns`
- Dynamic responses with `answers`
- Multiple responses with `andThen`
- Exception throwing with `throws`
- Coroutines with `coAnswers`


This migration can be easily done with a `Replace All` in your IDE, replacing `every` with `uponReceiving`.

#### Using with Mockito (Java)

To use pact-jvm-mock with Mockito in Java, simply replace all your `Mockito.when` calls with `PactMockito.uponReceiving`:

```java
import static io.github.ludorival.pactjvm.mock.mockito.PactMockito.uponReceiving;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// Simple response
uponReceiving(restTemplate.getForEntity(any(String.class), eq(UserProfile.class)))
    .thenReturn(ResponseEntity.ok(USER_PROFILE));

// With error response
uponReceiving(restTemplate.postForEntity(any(URI.class), any(), eq(UserProfile.class)))
    .withDescription("Create user profile - validation error")
    .given((builder, call) -> builder.state("Invalid user data", Collections.emptyMap()))
    .thenThrow(HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    errorMessage,
                    new HttpHeaders(),
                    errorMessage.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));
```

You can also add optional configurations like descriptions, provider states, and matching rules:

```java
uponReceiving(restTemplate.exchange(
        any(URI.class),
        eq(HttpMethod.GET),
        any(),
        any(ParameterizedTypeReference.class)
    ))
    .withDescription("List users")
    .given((builder, call) -> builder.state("Users exist", Collections.emptyMap()))
    .matchingRequest(rules -> rules.header("Authorization", new RegexMatcher("Bearer .*")))
    .matchingResponse(rules -> rules.body("[*].id", TypeMatcher.INSTANCE))
    .thenReturn(ResponseEntity.ok(Arrays.asList(USER_1, USER_2)));
```

Note: Don't forget to configure your test class with the `@EnablePactMock` annotation:

```java
@EnablePactMock(MyPactConfiguration.class)
public class MyTest {
    public static class MyPactConfiguration extends PactConfiguration {
        
        public MyPactConfiguration() {
            super("my-consumer", new SpringRestTemplateMockAdapter());
        }
    }
    // ... test methods
}
```

**That's it !!**

Run your tests, and you should see the generated pact files in your `src/test/resources/pacts`.

## More configurations

### Set a description

By default, the description is building from the current test name.You can set a description for each interaction using either `withDescription` or as part of the response chain:

```kotlin
uponReceiving {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} withDescription { "get the user profile" } returns ResponseEntity.ok(USER_PROFILE)

```

### Set provider states

The provider state refers to the state of the API or service that is being tested.
You can specify provider states using the `given` method:

```kotlin
uponReceiving {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} given { 
    state("The user has a preferred shopping list") 
} returns ResponseEntity.ok(USER_PROFILE)
```

### Configure matching rules

You can specify matching rules for both requests and responses using `matchingRequest` and `matchingResponse`:

```kotlin
uponReceiving {
    restTemplate.exchange(
        match<URI> { it.path.contains("user-service") },
        HttpMethod.GET,
        any(),
        any<ParameterizedTypeReference<List<User>>>()
    )
} matchingRequest {
    header("Authorization", RegexMatcher("Bearer .*"))
} matchingResponse {
    body("[*].id", TypeMatcher)
} returns ResponseEntity.ok(listOf(USER_1, USER_2))
```
In this example:
- The request matching rule ensures the Authorization header matches the pattern "Bearer" followed by any string
- The response matching rule specifies that each user ID in the array should match by type rather than exact value

### Client error

`pact-jvm-mock` offers also a way to record Http errors thanks to the `throws anError()` method:

```kotlin
uponReceiving {
    restTemplate.postForEntity(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList::class.java)
    ) given {
        state("The request should return a 400 Bad request") 
    } throws anError(ResponseEntity.badRequest().body("The title contains unexpected character")) 
```

### Configure custom JSON ObjectMapper

You can specify a custom ObjectMapper for serializing request/response bodies for specific providers. This is useful when you need special serialization handling, like custom date formats or naming strategies.

```kotlin
// MyServicePactConfig.kt
object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockAdapter({providerName ->
    Jackson2ObjectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializerByType(
            LocalDate::class.java,
            serializerAsDefault<LocalDate>("2023-01-01")
        ).build()
})) {

} 

```

### Make your contract deterministic

When working with Pact contracts, it's important to ensure that your tests are deterministic - meaning they produce the same output every time they run. This is particularly important when dealing with dynamic data like timestamps, UUIDs, or any other values that change between test runs.

There are two ways to make your contract deterministic:

1. **Enable deterministic mode globally**

Set `isDeterministic = true` in your `PactConfiguration`. When enabled, if the same interaction is recorded with different responses, the test will fail with an `IllegalStateException`:

```kotlin
object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockAdapter()) {
    override fun isDeterministic() = true // Enable deterministic mode
}
```

2. **Handle dynamic values with custom serializers**

For specific fields that are naturally dynamic (like dates or IDs), you can provide custom serializers to ensure consistent values:

```kotlin
object MyServicePactConfig : PactConfiguration(
    "my-service",
    SpringRestTemplateMockAdapter({ providerName ->
        Jackson2ObjectMapperBuilder()
            .serializerByType(
                LocalDateTime::class.java,
                serializerAsDefault<LocalDateTime>("2023-01-01T00:00:00")
            )
            .serializerByType(
                UUID::class.java,
                serializerAsDefault<UUID>("123e4567-e89b-12d3-a456-426614174000")
            )
            .build()
    })
) {
    override fun isDeterministic() = true
}
```

When deterministic mode is enabled:
- The same interaction must always return the same response
- If an interaction changes, the test will fail with a detailed error message showing the differences
- You must provide unique descriptions for different interactions using `withDescription`
- Dynamic values should be handled with custom serializers

This ensures your contract tests are reliable and reproducible across different test runs.

### Change the pact directory

By default, the generated pacts are stored in `src/test/resources/pacts`. You can configure that in the pact options:

```kotlin
// MyServicePactConfig.kt
object MyServicePactConfig : PactConfiguration("my-service") {

    ...
    override fun getPactDirectory() = "my-own-directory"
} 

```

## Contributing

pact-jvm-mock is an open-source project and contributions are welcome! If you're interested in contributing, please
check out the [contributing guidelines](CONTRIBUTING.md).

## License

pact-jvm-mock is licensed under the [MIT License](LICENSE).
