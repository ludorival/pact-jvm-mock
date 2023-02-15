Pact JVM Mock
=========================
![Build status](https://github.com/ludorival/pact-jvm-mock/actions/workflows/build.yaml/badge.svg)
![Publish status](https://github.com/ludorival/pact-jvm-mock/actions/workflows/publish.yaml/badge.svg)
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

<dependency>
    <groupId>io.github.ludorival</groupId>
    <artifactId>pact-jvm-mockk-spring</artifactId>
    <version>${pactJvmMockVersion}</version>
    <scope>test</scope>
</dependency>
````

### Configure the pacts

Next, you'll need to configure the library to use your existing [Mockk](https://github.com/mockk/mockk) mocks.

For example, let's say you want to leverage existing mock of Spring RestTemplate.
With JUnit 5, you can create an extension by inheriting `SpringPactMockkExtension`. Here is a minimal example:

```kotlin
import io.github.ludorival.pactjvm.mockk.pactOptions
import io.github.ludorival.pactjvm.mockk.spring.SpringRestTemplateMockkAdapter
import io.github.ludorival.pactjvm.mockk.writePacts

object MyPactMock : AfterAllCallback {

    init {
        pactOptions {
            provider = "my-service"
            // allow to intercept Spring RestTemplate mocks
            addAdapter(SpringRestTemplateMockkAdapter())
        }
    }

    override fun afterAll(context: ExtensionContext?) = writePacts()
} 
```

> By default, the contracts will be written in the src/test/resources/pacts folder.

### Extend your tests files

Then, to start writing contract,
you have to extend your test classes where you need to record the interactions with your consumers. Like that

```kotlin
@ExtendsWith(MyPactMock::class)
class ShoppingServiceClientTest 
```

### Migrate lightly your existing mocks

Finally, you can use the library to generate your Pact contracts.
`pact-jvm-mock` simplified the work by creating some extensions of [Mockk](https://github.com/mockk/mockk) functions.

You can replace all your :

- `returns` with `willRespond`
- `answers` with `willRespondWith`

Example:

- An existing mock of `restTemplate` returning static response.

```kotlin
every {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} returns ResponseEntity.ok(
    USER_PROFILE
)
```

- becomes

```kotlin
every {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} willRespond ResponseEntity.ok(
    USER_PROFILE
)
```

- Or a mock using `answers`

```kotlin
every {
    restTemplate.patchForObject(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList.Item::class.java)
    )
} answers {
    val item = arg<ShoppingList.Item>(1)
    item
}
```

- becomes

```kotlin
every {
    restTemplate.patchForObject(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList.Item::class.java)
    )
} willRespondWith {
    val item = arg<ShoppingList.Item>(1)
    item
}
```

Those changes can be easily done with a `Replace All` with your favourite IDE.

**That's it !!**

Run your tests, and you should see the generated pact files in your `src/test/resources/pacts`.

## More configurations

### Set a description

By default, the description is the test method name. You can set a description for each interaction.

Use `willRespondWith` like

```kotlin
every {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} willRespondWith {
    options {
        description = "get the user profile"
    }
    ResponseEntity.ok(
        USER_PROFILE
    )
}

```

### Set provider states

The provider state refers to the state of the API or service that is being tested.
The provider state is the starting point for each test scenario, and it sets the conditions under which the provider
will respond to requests from the consumer.

To specify the provider states :

```kotlin
every {
    restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} willRespondWith {
    options {
        description = "get the user profile"
        providerStates = listOf("The user has a preferred shopping list")
    }
    ResponseEntity.ok(
        USER_PROFILE
    )
}

```

### Client error

`pact-jvm-mock` offers also a way to record Http errors. Like

```kotlin
every {
    restTemplate.postForEntity(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList::class.java)
    )
} willRespondWith {
    options {
        providerStates = listOf("The request should return a 400 Bad request")
    }
    throw HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST,
        "The title contains unexpected character",
        HttpHeaders.EMPTY,
        null,
        null
    )
}
```

### Make your contract deterministic

If the Pact contracts change everytime tests have launched,
it makes overload your CICD by verifying the contracts upon the providers whereas nothing functional as changed.
To make your contract deterministic, you will need to provide a custom serializer for the type you want to be invariant.

For example, let's say you have a date which will be generated at each test, you can pass a custom value
for `determineConsumerFromUrl`

```kotlin
// MyPactMock.kt
init {
    pactOptions {
        provider = "my-service"
        isDeterministic = true // <-- force to be deterministic
        addAdapter(SpringRestTemplateMockkAdapter())
        objectMapperCustomizer = {
            Jackson2ObjectMapperBuilder().serializerByType(
                LocalDate::class.java,
                serializerAsDefault<LocalDate>("2023-01-01")
            ).build()
        }
    }
}

```

### Change the pact directory

By default, the generated pacts are stored in `src/test/resources/pacts`. You can configure that in the pact options:

```kotlin
// MyPactMock.kt
init {
    pactOptions {
        provider = "my-service"
        pactDirectory = "my-own-directory"
        // ...
    }
}

```

## Contributing

pact-jvm-mock is an open-source project and contributions are welcome! If you're interested in contributing, please
check out the [contributing guidelines](CONTRIBUTING.md).

## License

pact-jvm-mock is licensed under the [MIT License](LICENSE).
