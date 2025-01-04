Pact JVM Mock
=========================
![Build status](https://github.com/ludorival/pact-jvm-mock/actions/workflows/main.yaml/badge.svg)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/ludorival/pact-jvm-mock)
> A Kotlin library that leverages existing mocks to create Pact contract files, using the popular mocking
> library [Mockk](https://github.com/mockk/mockk).

## Motivation

Pact is a powerful tool for ensuring the compatibility of microservices. It allows you to define contracts between your
services and test them in isolation.

However, writing these contracts can be time-consuming and repetitive. This is where pact-jvm-mock comes in. It
automatically generates Pact contracts from your existing [Mockk](https://github.com/mockk/mockk) mocks, saving you time
and reducing the risk of human error.

## Features

- Automatically generate Pact contracts from existing [Mockk](https://github.com/mockk/mockk) mocks
- Supports all common mock interactions, such as method calls and property accesses
- Compatible with Spring Rest Template client and fully extensible
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

````xml
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
````

### Configure the pacts

Next, you'll need to configure the library to use your existing [Mockk](https://github.com/mockk/mockk) mocks.

For example, let's say you want to leverage existing mock of Spring RestTemplate.
Create a Kotlin object (or use an existing one) implementing `PactConfiguration`. Here is a minimal example:

```kotlin
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockkAdapter

object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockkAdapter())
```

### Extend your tests files

Then, to start writing contract,
you have to extend your test classes where you need to record the interactions with your providers. Like that

```kotlin
import io.github.ludorival.pactjvm.mock.mockk.PactConsumer

@PactConsumer(MyServicePactConfig::class)
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
    .withProviderState("Invalid user data", Collections.emptyMap())
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
    .withProviderState("Users exist", Collections.emptyMap())
    .withRequestMatchingRules(rules -> {
        rules.header("Authorization", new Matcher(Matcher.MatchEnum.REGEX, "Bearer .*"));
    })
    .withResponseMatchingRules(rules -> {
        rules.body("[*].id", new Matcher(Matcher.MatchEnum.TYPE));
    })
    .thenReturn(ResponseEntity.ok(Arrays.asList(USER_1, USER_2)));
```

Note: Don't forget to configure your test class with the `@PactConsumer` annotation:

```java
@PactConsumer(MyPactConfiguration.class)
public class MyTest {
    public static class MyPactConfiguration extends PactConfiguration {
        
        public MyPactConfiguration() {
            super("my-consumer", new SpringRestTemplateMockkAdapter());
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
} withDescription "get the user profile" returns ResponseEntity.ok(USER_PROFILE)

```

### Set provider states

The provider state refers to the state of the API or service that is being tested.
You can specify provider states using the `given` method:

```kotlin
uponReceiving {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} given state("The user has a preferred shopping list") returns ResponseEntity.ok(USER_PROFILE)
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
    header("Authorization", Matcher(Matcher.MatchEnum.REGEX, "Bearer .*"))
} matchingResponse {
    body("[*].id", Matcher(Matcher.MatchEnum.TYPE))
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
    ) given state("The request should return a 400 Bad request") throws anError(ResponseEntity.badRequest().body("The title contains unexpected character")) 
```

### Configure custom JSON ObjectMapper

You can specify a custom ObjectMapper for serializing request/response bodies for specific providers. This is useful when you need special serialization handling, like custom date formats or naming strategies.

```kotlin
// MyServicePactConfig.kt
object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockkAdapter()) {

    override fun customizeObjectMapper(providerName: String) = Jackson2ObjectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializerByType(
            LocalDate::class.java,
            serializerAsDefault<LocalDate>("2023-01-01")
        ).build()
} 

```

### Make your contract deterministic

To make your contract deterministic, you will need to provide a custom serializer for the type you want to be invariant.

For example, let's say you have a date which will be generated at each test, you can pass a custom value
for `determineConsumerFromUrl`


```kotlin
// MyServicePactConfig.kt
object MyServicePactConfig : PactConfiguration("my-service", SpringRestTemplateMockkAdapter()) {

    override fun isDeterministic() = true // <-- force to be deterministic
    override fun customizeObjectMapper(providerName: String) = Jackson2ObjectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .serializerByType(
            LocalDate::class.java,
            serializerAsDefault<LocalDate>("2023-01-01")
        ).build()
} 
```


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
