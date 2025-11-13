# Pact-JVM-Mock Migration Guide

Migrate existing JVM backend test mocks to generate Pact consumer contracts using pact-jvm-mock.

## Prerequisites

**Required:**
- Build tool: Maven (pom.xml) OR Gradle (build.gradle/build.gradle.kts)
- Mocking framework: Mockito OR MockK
- Language: Java OR Kotlin
- Test files with HTTP client mocks
- JVM-based project

**Optional:**
- Spring RestTemplate (fully supported)
- JUnit 4/5, TestNG

**If prerequisites not met:** STOP and explain why migration cannot proceed.

## Migration Steps

### Step 1: Analyze Codebase

Determine:
- Build tool (Maven/Gradle), mocking framework (Mockito/MockK), language (Java/Kotlin)
- HTTP client (RestTemplate/WebClient/OkHttp)
- Consumer name: Spring apps → `application.yml/properties` (`spring.application.name`); others → artifactId/project name
- Provider names: Infer from service URLs
- Test location: `src/test/java` or `src/test/kotlin`
- Test files with mocks to migrate

### Step 2: Identify Files & Pact Broker

1. **List test files** with mocks (show paths, Mockito vs MockK, RestTemplate usage)
2. **Ask user:**
   - Which files to migrate? (all/specific/single file/pattern)
   - Do you have a Pact Broker URL? (Required for Steps 9-10)
3. **If no Pact Broker URL:**
   - Inform: Steps 9-10 cannot complete without it
   - Reference: [Pact Broker docs](https://docs.pact.io/pact_broker), [Docker image](https://hub.docker.com/r/pactfoundation/pact-broker)
   - Note: Steps 3-8 can proceed; 9-10 later
4. **Validate files** exist, contain mocks, are test files
5. **Wait for confirmation** before migration

### Step 3: Add Dependencies

**Gradle:**
```groovy
testImplementation "io.github.ludorival:pact-jvm-mock-mockito:1.4.0"
// OR
testImplementation "io.github.ludorival:pact-jvm-mock-mockk:1.4.0"
// If using Spring RestTemplate:
testImplementation "io.github.ludorival:pact-jvm-mock-spring:1.4.0"
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.ludorival</groupId>
    <artifactId>pact-jvm-mock-mockito</artifactId>
    <version>1.4.0</version>
    <scope>test</scope>
</dependency>
<!-- OR pact-jvm-mock-mockk for MockK -->
<!-- Add pact-jvm-mock-spring if using Spring RestTemplate -->
```

### Step 4: Create PactConfiguration

**Kotlin:**
```kotlin
import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter

object MyServicePactConfig : PactConfiguration(
    SpringRestTemplateMockAdapter("my-consumer") // Consumer name from application.yml/properties or artifactId
)
```

**Java:**
```java
import io.github.ludorival.pactjvm.mock.PactConfiguration;
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter;

public static class MyPactConfiguration extends PactConfiguration {
    public MyPactConfiguration() {
        super(new SpringRestTemplateMockAdapter("my-consumer"));
    }
}
```

**Consumer name:** Spring apps → `application.yml`/`application.properties` (`spring.application.name`); others → artifactId/project name

**Provider names:** Infer from service URLs (e.g., "user-service", "shopping-service")

**Advanced:**
- Custom ObjectMapper: Pass mapper builder to `SpringRestTemplateMockAdapter`
- Custom Pact directory: Override `getPactDirectory()`

### Step 5: Migrate Test Classes

For each test file:

1. **Add annotation:**
   - Kotlin: `@EnablePactMock(MyServicePactConfig::class)`
   - Java: `@EnablePactMock(MyPactConfiguration.class)`

2. **Add imports:**
   - `import io.github.ludorival.pactjvm.mock.EnablePactMock`
   - MockK: `import io.github.ludorival.pactjvm.mock.mockk.uponReceiving`
   - Mockito: `import static io.github.ludorival.pactjvm.mock.mockito.PactMockito.uponReceiving;`
   - For provider states (Mockito): `import io.github.ludorival.pactjvm.mock.ProviderStateBuilder`
   - Note: For MockK, `given` and `state` are available without additional imports (infix functions and methods)

3. **Replace mocks:**

**MockK (Kotlin):**
```kotlin
// Before
every { restTemplate.getForEntity(...) } returns ResponseEntity.ok(...)

// After
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
uponReceiving { restTemplate.getForEntity(...) }
    given { state("service is available") }
    returns ResponseEntity.ok(...)

// Coroutines
coEvery { } → uponCoReceiving { }
```

**Mockito (Java):**
```java
// Before
when(restTemplate.getForEntity(...)).thenReturn(ResponseEntity.ok(...));

// After
import static io.github.ludorival.pactjvm.mock.mockito.PactMockito.uponReceiving;
import io.github.ludorival.pactjvm.mock.ProviderStateBuilder;
uponReceiving(restTemplate.getForEntity(...))
    .given(builder -> builder.state("service is available"))
    .thenReturn(ResponseEntity.ok(...));
```

**Key Points:**
- 100% backward compatible; tests pass unchanged
- Keep all matchers, responses, exceptions unchanged
- Test assertions remain unchanged
- Incremental migration supported (mix `every`/`when` with `uponReceiving`)
- IDE "Replace All" works for MockK (`every` → `uponReceiving`)

**Supported Features:**
- Static responses: `returns value` / `thenReturn(value)`
- Dynamic responses: `answers { }` / `thenAnswer(answer)`
- Multiple responses: `andThen value` / `thenReturn(v1, v2, ...)`
- Exceptions: `throws exception` / `thenThrow(exception)`
- HTTP errors: `throws anError(ResponseEntity.badRequest().body(...))` (MockK: `import io.github.ludorival.pactjvm.mock.anError`)
- Custom descriptions: `withDescription { "name" }` / `.withDescription("name")`
- Matching rules: `matchingRequest { } matchingResponse { }` / `.matchingRequest(...).matchingResponse(...)`

4. **Add provider states (MANDATORY):**
   - **Every interaction MUST have a valid provider state**
   - See "Provider States Guidance" below for best practices and examples

**Provider States Guidance:**

**Provider states are MANDATORY:**
- **Every `uponReceiving` interaction MUST include a provider state**
- Provider states describe the state the provider must be in to return the expected response
- Even for static responses (e.g., health checks), provide a descriptive state (e.g., "service is healthy", "service is available")
- Provider states help providers understand what setup is needed for verification

**Avoid multiplying states - Best practices:**
- **Reuse states:** Use the same state name for similar scenarios (e.g., "user exists" for multiple user-related interactions)
- **Use parameters:** Pass parameters to states instead of creating multiple similar states (e.g., `state("user exists", Map.of("userId", "123"))` instead of separate states for each user)
- **Consolidate similar states:** Combine states that set up similar data (e.g., "user exists" instead of "user exists with profile", "user exists with preferences")
- **Use descriptive names:** Name states based on what they represent, not test-specific details (e.g., "user exists" not "test user 123 exists")
- **One state per scenario:** Create one state per business scenario, not one per test method

**Examples:**

**Good - Reusing states with parameters:**
```java
// Multiple interactions can use the same state with different parameters
uponReceiving(...).given(builder -> builder.state("user exists", Map.of("userId", "123"))).thenReturn(...);
uponReceiving(...).given(builder -> builder.state("user exists", Map.of("userId", "456"))).thenReturn(...);
```

**Bad - Creating separate states for each interaction:**
```java
// Don't do this - creates unnecessary states
uponReceiving(...).given(builder -> builder.state("user 123 exists")).thenReturn(...);
uponReceiving(...).given(builder -> builder.state("user 456 exists")).thenReturn(...);
uponReceiving(...).given(builder -> builder.state("user 789 exists")).thenReturn(...);
```

**Good - Always including provider states:**
```java
// Always include a provider state, even for static responses
uponReceiving(...).given(builder -> builder.state("service is healthy"))
    .thenReturn(ResponseEntity.ok("Health check OK"));

// State with parameters for dynamic responses
uponReceiving(...).given(builder -> builder.state("user exists", Map.of("userId", "123")))
    .thenReturn(ResponseEntity.ok(userProfile));
```

**Bad - Missing provider states:**
```java
// Don't do this - provider state is mandatory
uponReceiving(...).thenReturn(ResponseEntity.ok("OK"));
```

**Migration workflow:**
1. Replace `every`/`when` with `uponReceiving` (step 3 above)
2. Add provider states to ALL interactions (step 4 above) - **MANDATORY**
3. Consolidate similar states into reusable states with parameters
4. Run tests and verify Pact files are generated (Step 6)

### Step 6: Verify (MANDATORY)

**Run tests:**
- Gradle: `./gradlew test`
- Maven: `mvn test`
- Verify tests pass without assertion changes

**Verify Pact files:**
- Location: `src/test/resources/pacts` (or custom directory)
- Pattern: `{consumer-name}-{provider-name}.json`
- Validate: consumer/provider names, interactions (one per `uponReceiving`), **provider states present for all interactions**, request/response details

**Troubleshooting:**
- Verify `@EnablePactMock` annotation, `PactConfiguration` setup, dependencies added
- Check test execution logs and ensure `src/test/resources/pacts` directory exists

**If tests fail or no Pact files:** STOP and fix before proceeding.

**Add to `.gitignore`:**
   ```
   # Pact contract files (generated by pact-jvm-mock)
   src/test/resources/pacts/
   ```
   
### Step 7: Optional Enhancements

Refer to Step 5 for syntax. Add if they improve contract clarity:

1. **Custom descriptions:** `withDescription { "name" }` / `.withDescription("name")`
2. **Matching rules:** `matchingRequest { } matchingResponse { }` / `.matchingRequest(...).matchingResponse(...)`
3. **Error handling:** `throws anError(ResponseEntity.badRequest().body(...))` / `thenThrow(anError(...))`

**Note:** Provider states are mandatory and handled in Step 5 as part of the migration process. Only add other enhancements if they improve contract clarity or test coverage.

### Step 8: Deterministic Mode (Advanced - Optional)

**Do NOT enable during initial migration.**

**Enable after:**
- Step 6 complete
- Steps 10-11 complete (Pact Broker configured)
- CI/CD needs consistent contracts

**Kotlin:**
```kotlin
object MyServicePactConfig : PactConfiguration(...) {
    override fun isDeterministic() = true
}
```

**Java:**
```java
    @Override
    public boolean isDeterministic() {
    return true;
}
```

**Requirements:**
- Unique descriptions for all interactions (`withDescription`)
- Handle dynamic values (timestamps, UUIDs) with custom serializers
- Tests fail if interactions change

### Step 9: Configure Pact Broker (MANDATORY)

**Prerequisites:** Step 6 complete, Pact Broker URL from Step 2

**If no Pact Broker URL:** STOP, inform user, reference docs. Steps 9-10 cannot complete.

**Gradle:**

1. Add plugin:
   ```groovy
   plugins {
       id 'au.com.dius.pact' version '4.6.0'
   }
   ```

2. Configure publish:
   ```groovy
   pact {
       publish {
        pactDirectory = file('src/test/resources/pacts')
           pactBrokerUrl = System.getenv('PACT_BROKER_URL') ?: 'http://your-pact-broker-url'
        pactBrokerToken = System.getenv('PACT_BROKER_TOKEN')
        tags = [System.getenv('GIT_BRANCH') ?: 'main']
           // version = System.getenv('GIT_COMMIT') ?: project.version
       }
   }
   ```

3. Publish: `./gradlew pactPublish`

**Maven:**

1. Add plugin:
   ```xml
           <plugin>
               <groupId>au.com.dius.pact.provider</groupId>
               <artifactId>maven</artifactId>
               <version>4.6.0</version>
               <configuration>
                   <pactDirectory>${project.basedir}/src/test/resources/pacts</pactDirectory>
                   <pactBrokerUrl>${env.PACT_BROKER_URL}</pactBrokerUrl>
                   <pactBrokerToken>${env.PACT_BROKER_TOKEN}</pactBrokerToken>
        <tags><tag>${env.GIT_BRANCH}</tag></tags>
                   <projectVersion>${env.GIT_COMMIT}</projectVersion>
               </configuration>
           </plugin>
   ```

2. Publish: `mvn pact:publish`

**4. Add can-i-deploy check (RECOMMENDED):**

The `can-i-deploy` check verifies that all consumer contracts are compatible with provider versions before deployment. This prevents deploying incompatible versions.

**Gradle:**

Add can-i-deploy task configuration:
```groovy
pact {
    publish {
        pactDirectory = file('src/test/resources/pacts')
        pactBrokerUrl = System.getenv('PACT_BROKER_URL') ?: 'http://your-pact-broker-url'
        pactBrokerToken = System.getenv('PACT_BROKER_TOKEN')
        tags = [System.getenv('GIT_BRANCH') ?: 'main']
        // version = System.getenv('GIT_COMMIT') ?: project.version
    }
    serviceProviders {
        // Configure can-i-deploy for each provider
        'provider-name' {
            pactBrokerUrl = System.getenv('PACT_BROKER_URL')
            pactBrokerToken = System.getenv('PACT_BROKER_TOKEN')
        }
    }
}
```

Run can-i-deploy: `./gradlew pactCanIDeploy -Ppact.provider=provider-name -Ppact.consumer=consumer-name -Ppact.consumerVersion=version`

**Maven:**

Add can-i-deploy goal configuration:
```xml
<plugin>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>maven</artifactId>
    <version>4.6.0</version>
    <configuration>
        <pactDirectory>${project.basedir}/src/test/resources/pacts</pactDirectory>
        <pactBrokerUrl>${env.PACT_BROKER_URL}</pactBrokerUrl>
        <pactBrokerToken>${env.PACT_BROKER_TOKEN}</pactBrokerToken>
        <tags><tag>${env.GIT_BRANCH}</tag></tags>
        <projectVersion>${env.GIT_COMMIT}</projectVersion>
    </configuration>
    <executions>
        <execution>
            <id>can-i-deploy</id>
            <goals>
                <goal>can-i-deploy</goal>
            </goals>
            <configuration>
                <pactBrokerUrl>${env.PACT_BROKER_URL}</pactBrokerUrl>
                <pactBrokerToken>${env.PACT_BROKER_TOKEN}</pactBrokerToken>
                <pacticipant>consumer-name</pacticipant>
                <pacticipantVersion>${env.GIT_COMMIT}</pacticipantVersion>
                <to>production</to>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Run can-i-deploy: `mvn pact:can-i-deploy`

**For CI/CD:** Use the Maven or Gradle commands above (no additional CLI installation required).

**Alternative: Use Pact CLI (if Maven/Gradle not available):**

Install Pact CLI and use it directly:
```bash
# After pact publish
pact-broker can-i-deploy \
  --pacticipant consumer-name \
  --version ${GIT_COMMIT} \
  --to production \
  --broker-base-url ${PACT_BROKER_URL} \
  --broker-token ${PACT_BROKER_TOKEN}
```

**When to use:**
- Before deploying to production/staging environments
- In CI/CD pipelines after pact publishing
- To verify contract compatibility before release

**Environment Variables:**
- `PACT_BROKER_URL`: Broker URL (required)
- `PACT_BROKER_TOKEN`: Auth token (required)
- `GIT_COMMIT`: Commit hash for versioning (recommended)
- `GIT_BRANCH`: Branch name for tagging (optional)

**Notes:**
- Plugin version and property names may vary; check [Pact docs](https://docs.pact.io/implementation_guides/jvm)
- Run tests first to generate Pact files before publishing

### Step 10: Update CI/CD Pipeline (MANDATORY)

**Prerequisites:** Steps 6 and 9 complete

1. Detect existing pipeline (GitHub Actions, Jenkins, GitLab CI, etc.)
2. Ask for Pact Broker URL/token if not in configuration
3. Add/modify steps:
   - Run tests: `mvn test` or `./gradlew test`
   - Publish contracts: `mvn pact:publish` or `./gradlew pactPublish`
   - **can-i-deploy check (RECOMMENDED):** After publishing, verify contract compatibility before deployment
     - Gradle: `./gradlew pactCanIDeploy -Ppact.provider=provider-name -Ppact.consumer=consumer-name -Ppact.consumerVersion=${GIT_COMMIT}`
     - Maven: `mvn pact:can-i-deploy`
4. Set environment variables: `PACT_BROKER_URL`, `PACT_BROKER_TOKEN`, `GIT_COMMIT` (for versioning), `GIT_BRANCH` (optional)

**Example CI/CD pipeline flow:**
1. Run tests → Generate Pact files
2. Publish contracts to Pact Broker
3. **can-i-deploy check** → Verify compatibility
4. Deploy (only if can-i-deploy passes)

### Step 11: Summary

Provide:
- Migrated files (from Step 2)
- Changes made to each file
- Step 6 verification (tests passed, Pact files generated)
- Pact file location
- Pact Broker configuration (if URL available)
- CI/CD configuration (if URL available)
- **If no Pact Broker URL:** Mark migration INCOMPLETE, list remaining steps (9-10), reference docs
- Next steps (extend migration, add enhancements)
- `.gitignore` configured for Pact files

## Key Points

- **Backward compatible:** Tests pass without assertion changes
- **Mandatory steps:** 6 (verify), 9 (Pact Broker), 10 (CI/CD)
- **Incremental migration:** Safe to migrate one test at a time
- **Pact files:** Auto-generated in `src/test/resources/pacts`, published to broker, ignored in Git
- **Provider states:** **MANDATORY** for all interactions; avoid multiplying states; reuse states with parameters
- **Deterministic mode:** Advanced feature; enable only after Steps 6, 9-10 complete
