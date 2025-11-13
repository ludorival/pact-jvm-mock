# Pact Provider Verification Guide

Verify that your provider service implements the contracts shared by consumers using pact-jvm provider verification.

## Prompt Variable: Obtain Pact File Path

**🚨 LLM AGENT INSTRUCTION: REQUEST PACT FILE PATH 🚨**

**Before proceeding with any steps, you MUST:**

1. **Ask the user for the local consumer pact file path:**
   - Prompt: "Please provide the path to the local consumer Pact contract file that needs to be copied into the provider project."
   - Wait for the user to provide the file path
   - Do NOT proceed until the user provides the path

2. **Once the path is provided:**
   - Verify the file exists at the provided path
   - Check that it is a valid JSON file
   - Confirm the file naming pattern matches `{consumer-name}-{provider-name}.json`
   - If the file is valid, proceed to copy it to `src/test/resources/pacts/` in the provider project
   - If the file is invalid or doesn't exist, ask the user to provide a valid path

3. **Copy the pact file to provider project:**
   - Create the directory `src/test/resources/pacts/` if it doesn't exist
   - Copy the pact file from the provided path to `src/test/resources/pacts/`
   - Preserve the original filename
   - Verify the file was copied successfully

4. **After copying:**
   - Confirm with the user that the file has been copied
   - Proceed to Step 1: Prerequisites

**Example interaction:**
```
LLM: "Please provide the path to the local consumer Pact contract file that needs to be copied into the provider project."

User: "/path/to/consumer-project/target/pacts/shop-frontend-order-service.json"

LLM: [Copies file to src/test/resources/pacts/shop-frontend-order-service.json]
     "Pact file copied successfully. Proceeding with verification setup..."
```

## Step 1: Prerequisites

**🚨 MANDATORY STOP CONDITION FOR LLM AGENTS 🚨**

**IF PACT FILES ARE NOT FOUND: STOP IMMEDIATELY. DO NOT PROCEED WITH VERIFICATION.**

**Note:** If you have not yet obtained the pact file path from the user, go back to the "Prompt Variable: Obtain Pact File Path" section above and complete that step first.

**Before proceeding with any verification steps, you MUST:**
1. Verify that the pact file was successfully copied to `src/test/resources/pacts` directory (from the previous step)
2. Check if Pact contract files exist in `src/test/resources/pacts` directory
3. Verify that at least one Pact file (JSON) is present with the pattern `{consumer-name}-{provider-name}.json`
4. If no Pact files are found: **STOP** and inform the user that Pact files from the consumer team are required
5. Do NOT proceed to Step 2 or any subsequent steps until Pact files are available
6. Do NOT attempt to create mock Pact files or proceed without actual contract files

**Required:**
- **Pact contract files from consumer:** The consumer team MUST generate and share Pact contract files (JSON) before provider verification can proceed. Without these files, verification CANNOT be performed.
  - Location: `src/test/resources/pacts/`
  - Format: JSON files with naming pattern `{consumer-name}-{provider-name}.json`
  - Example: `shop-frontend-order-service.json`
- Build tool: Maven (pom.xml) OR Gradle (build.gradle/build.gradle.kts)
- Language: Kotlin (preferred) OR Java
- Testing framework: JUnit 5 (recommended) OR JUnit 4
- Provider service: Runnable service (Spring Boot, standalone, etc.)

**Optional:**
- Spring Boot (fully supported with Spring integration)
- Pact Broker: For automated contract verification workflows

**If Pact files are not available:**
- **STOP verification process immediately**
- Inform the user that Pact contract files from the consumer team are required
- Request the user to contact the consumer team to obtain the Pact contract files
- Do NOT proceed with any verification steps until Pact files are provided

## Step 2: Analyze Consumer Contract Tests

**🚨 VERIFICATION CHECK: PACT FILES MUST EXIST 🚨**

**Before proceeding, verify Pact files exist:**
1. Check if directory `src/test/resources/pacts` exists
2. Check if at least one JSON file exists in that directory
3. Verify file naming pattern: `{consumer-name}-{provider-name}.json`

**IF NO PACT FILES ARE FOUND:**
- **STOP immediately**
- Inform the user: "Pact contract files are required but not found in src/test/resources/pacts"
- Request the user to obtain Pact files from the consumer team
- Do NOT proceed with analysis or any subsequent steps

**Read the Pact contract files to understand what needs to be verified:**

1. **Locate Pact files:**
   - Path: `src/test/resources/pacts/{consumer-name}-{provider-name}.json`
   - Example: `shop-frontend-order-service.json`
   - **If files are not found at this location, STOP and request Pact files from consumer team**

2. **Identify provider states:**
   - Look for `"providerState"` or `"providerStates"` fields in each interaction
   - Note state names (some interactions may not have provider states)

3. **Identify interactions:**
   - Review HTTP methods, paths, request/response bodies
   - Note required headers, query parameters, status codes

**Example Pact contract file:**
```json
{
  "consumer": {
    "name": "shop-frontend"
  },
  "provider": {
    "name": "order-service"
  },
  "interactions": [
    {
      "description": "GET /order-service/v1/items returns status 200",
      "request": {
        "method": "GET",
        "path": "/order-service/v1/items"
      },
      "response": {
        "status": 200,
        "body": [
          {
            "id": 1,
            "name": "Test Item 1",
            "description": "This is a test item",
            "stockCount": 5
          }
        ]
      }
    },
    {
      "description": "purchase item successfully",
      "providerStates": "item exists and is available",
      "request": {
        "method": "POST",
        "path": "/order-service/v1/purchase",
        "headers": {
          "content-type": "application/json"
        },
        "body": {
          "itemId": 1,
          "quantity": 3
        }
      },
      "response": {
        "status": 200
      }
    }
  ]
}
```

**From this contract, identify:**
- **Provider name:** `order-service` (must match `@Provider` annotation exactly)
- **Consumer name:** `shop-frontend`
- **Provider states:**
  - `"item exists and is available"` (for purchase interaction)
  - Note: Some interactions don't have provider states (GET /items)
- **Interactions:**
  - `GET /order-service/v1/items` → Returns list of items
  - `POST /order-service/v1/purchase` → With state "item exists and is available" → Returns 200

## Step 3: Implement Verification Tests

### 3.1: Add Dependencies

**Gradle (Kotlin):**
```groovy
testImplementation "au.com.dius.pact.provider:junit5:4.6.17"
// If using Spring Boot:
testImplementation "au.com.dius.pact.provider:spring:4.6.17"
// Kotlin test dependencies
testImplementation "org.jetbrains.kotlin:kotlin-test-junit5"
testImplementation "io.mockk:mockk:1.13.8"
```

**Maven (Kotlin):**
```xml
<dependency>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.17</version>
    <scope>test</scope>
</dependency>
<!-- If using Spring Boot: -->
<dependency>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>spring</artifactId>
    <version>4.6.17</version>
    <scope>test</scope>
</dependency>
<!-- Kotlin test dependencies -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-test-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk</artifactId>
    <version>1.13.8</version>
    <scope>test</scope>
</dependency>
```

### 3.2: Create Test Configuration

**Create a test configuration class to mock dependencies (if needed):**

```kotlin
// TestConfig.kt
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun mockedRestTemplate(): RestTemplate {
        return mockk<RestTemplate>()
    }
}
```

### 3.3: Create Verification Test Class

**Create the verification test class using Kotlin:**

```kotlin
import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("order-service")
@PactFolder("src/test/resources/pacts")
@Import(TestConfig::class)
class PactProviderVerificationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // Provider state handlers
    @State("item exists and is available")
    fun `item exists and is available`() {
        // Set up test data: create item with id=1 and stockCount >= requested quantity
        // Example: itemRepository.createItem(1, "Test Item 1", "This is a test item", 5)
        // Or use mocking: restTemplate.givenItemsAreAvailable(listOf(Item(1, "Test Item 1", "This is a test item", 5)))
    }
}
```

**Notes:**
- The example uses Kotlin backticks for state handler method names (e.g., `` `item exists and is available`() ``), which allows method names with spaces and matches the provider state name exactly.
- The `TestConfig` class provides a mocked `RestTemplate` that can be used with extension functions (e.g., `givenItemsAreAvailable()`, `givenItemBookingSucceeds()`) to set up test data. These extension functions are project-specific and need to be implemented based on your testing needs.
- System properties for publishing results and provider version are configured in the `setUp` method to support CI/CD integration.

### 3.4: Implement Provider State Handlers

**For each provider state identified in Step 2, implement a handler. Based on the example contract:**

```kotlin
@State("item exists and is available")
fun `item exists and is available`() {
    // Set up test data: create item with sufficient stock
    // Example using repository:
    // itemRepository.createItem(1, "Test Item 1", "This is a test item", 5)
    
    // Example using mocked RestTemplate with extension functions:
    // val mockItem = Item(1, "Test Item 1", "This is a test item", 5)
    // restTemplate.givenItemsAreAvailable(listOf(mockItem))
    // restTemplate.givenItemBookingSucceeds(1, 3)
}
```

**Key Points:**
- State name in `@State` annotation must match exactly (case-sensitive) with contract
- In this example: `"item exists and is available"` (no parameters in this contract)
- If contract has `providerStateParameters`, use `params: Map<String, Any>` to access them
- Set up test data that matches contract expectations
- One `@State` method per provider state
- Some interactions may not have provider states (like the GET /items interaction in this example)

## Step 4: Run the Verification Test

**Run verification tests:**
- Gradle: `./gradlew test` or `./gradlew test --tests PactProviderVerificationTest`
- Maven: `mvn test` or `mvn test -Dtest=PactProviderVerificationTest`

**Expected results:**
- All interactions from contracts are verified
- Provider state handlers are executed for each interaction
- Tests pass if provider implements contracts correctly

## Step 5: Fix Failures if Any

**Common failures and fixes:**

1. **Missing provider state handler:**
   - Error: `"No state change handler found for state: 'X'"`
   - Fix: Add `@State("X")` handler method in test class

2. **Provider state handler not setting up data correctly:**
   - Error: Response body is empty or doesn't match contract
   - Fix: Update state handler to set up correct test data

3. **Missing endpoint:**
   - Error: `404 Not Found`
   - Fix: Implement the endpoint in provider service

4. **Response mismatch:**
   - Error: Response body, headers, or status code doesn't match contract
   - Fix: Update provider implementation to match contract

5. **Consumer-side issues:**
   - Error: Contract expects something that violates provider API design
   - Action: **STOP** and inform consumer team. Do NOT modify provider to match incorrect contract.

**Iterative fix process:**
1. Run verification
2. Identify failure
3. Fix issue (provider-side) or inform consumer team (consumer-side)
4. Run verification again
5. Repeat until all tests pass

## Step 6: Configure Pact Broker (Optional)

**If using Pact Broker for automated verification, update verification test to load contracts from broker:**

### 6.1: Update Verification Test

**Replace `@PactFolder` with `@PactBroker`:**

```kotlin
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("order-service")
@PactBroker(
    url = "\${pactbroker.url}",
    authentication = PactBrokerAuth(token = "\${pactbroker.auth.token}")
)
@Import(TestConfig::class)
class PactProviderVerificationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
        
        // Configure publishing results (only publish in CI/CD, not locally)
        System.setProperty("pact.verifier.publishResults", System.getenv("CI") ?: "false")
        System.setProperty("pact.provider.version", System.getenv("GIT_COMMIT") ?: System.getenv("GITHUB_SHA") ?: "unknown")
        System.setProperty("pact.provider.branch", System.getenv("GIT_BRANCH") ?: System.getenv("GITHUB_REF_NAME") ?: "unknown")
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // Provider state handlers (same as before)
    @State("item exists and is available")
    fun `item exists and is available`() {
        // Set up test data
    }
}
```

### 6.2: Configure Application Properties

**application.yml:**
```yaml
pactbroker:
  url: ${PACT_BROKER_URL:http://localhost:9292}
  auth:
    token: ${PACT_BROKER_TOKEN:}
```

**application.properties:**
```properties
pactbroker.url=${PACT_BROKER_URL:http://localhost:9292}
pactbroker.auth.token=${PACT_BROKER_TOKEN:}
```

### 6.3: Configure Webhook (Pact Broker → CI/CD)

**Set up webhook in Pact Broker to trigger verification when contract is published:**

1. **In Pact Broker UI:**
   - Go to provider settings
   - Add webhook with URL: `https://your-ci-cd-url/webhook/pact-verification`
   - Configure webhook to trigger on: `contract:published`
   - Set webhook method: `POST`
   - Include consumer version tags, consumer version number, provider version tags

2. **Webhook payload example:**
```json
{
  "pact_url": "https://pact-broker/pacts/provider/order-service/consumer/shop-frontend/version/1.0.0",
  "consumer_version_tags": ["main"],
  "consumer_version_number": "1.0.0",
  "provider_version_tags": ["main"],
  "event_name": "contract_published"
}
```

## Step 7: Implement CI/CD Verification Job

**Create CI/CD job to run verification tests automatically.**

### 7.1: GitHub Actions Example

```yaml
name: Pact Contract Verification

on:
  workflow_dispatch:
    inputs:
      pact_url:
        description: 'URL of the pact to verify'
        required: true
      consumer_version_tags:
        description: 'Consumer version tags'
        required: false
        default: ''
      consumer_version_number:
        description: 'Consumer version number'
        required: false
        default: ''
      provider_version_tags:
        description: 'Provider version tags'
        required: false
        default: ''
      event_name:
        description: 'Name of the event that triggered the webhook'

permissions:
  checks: write
  contents: read

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Verify Pact
        env:
          PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
          CI: true
          GIT_COMMIT: ${{ github.sha }}
          GIT_BRANCH: ${{ github.ref_name }}
        run: ./mvnw test -Dtest=PactProviderVerificationTest
      
      - name: Test Report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Pact Verification Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit
          fail-on-error: true
```

### 7.2: Jenkins Example

```groovy
pipeline {
    agent any
    
    environment {
        PACT_BROKER_URL = credentials('pact-broker-url')
        PACT_BROKER_TOKEN = credentials('pact-broker-token')
        CI = 'true'
        GIT_COMMIT = "${env.GIT_COMMIT ?: 'unknown'}"
        GIT_BRANCH = "${env.GIT_BRANCH ?: env.BRANCH_NAME ?: 'unknown'}"
    }
    
    stages {
        stage('Verify Pact') {
            steps {
                sh './mvnw test -Dtest=PactProviderVerificationTest'
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

### 7.3: GitLab CI Example

```yaml
verify-pact:
  image: maven:3.9-eclipse-temurin-21
  variables:
    PACT_BROKER_URL: $PACT_BROKER_URL
    PACT_BROKER_TOKEN: $PACT_BROKER_TOKEN
    CI: "true"
    GIT_COMMIT: $CI_COMMIT_SHA
    GIT_BRANCH: $CI_COMMIT_REF_NAME
  script:
    - ./mvnw test -Dtest=PactProviderVerificationTest
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
```

### 7.4: Azure DevOps Example

```yaml
trigger: none

pool:
  vmImage: 'ubuntu-latest'

variables:
  PACT_BROKER_URL: $(PACT_BROKER_URL)
  PACT_BROKER_TOKEN: $(PACT_BROKER_TOKEN)
  CI: 'true'
  GIT_COMMIT: $(Build.SourceVersion)
  GIT_BRANCH: $(Build.SourceBranchName)

steps:
  - task: Maven@3
    inputs:
      mavenPomFile: 'pom.xml'
      goals: 'test'
      options: '-Dtest=PactProviderVerificationTest'
  
  - task: PublishTestResults@2
    inputs:
      testResultsFiles: 'target/surefire-reports/*.xml'
      testRunTitle: 'Pact Verification Tests'
```

### 7.5: Environment Variables

**Set in CI/CD secrets:**
- `PACT_BROKER_URL`: Pact Broker URL
- `PACT_BROKER_TOKEN`: Pact Broker authentication token

**Set in CI/CD environment (for publishing verification results):**
- `CI`: Set to `"true"` to enable publishing verification results (used by `pact.verifier.publishResults`)
- `GIT_COMMIT` or `GITHUB_SHA`: Commit hash for provider version (used by `pact.provider.version`)
- `GIT_BRANCH` or `GITHUB_REF_NAME`: Branch name for provider branch (used by `pact.provider.branch`)

**Platform-specific environment variables:**
- **GitHub Actions:** `GITHUB_SHA`, `GITHUB_REF_NAME`
- **GitLab CI:** `CI_COMMIT_SHA`, `CI_COMMIT_REF_NAME`
- **Jenkins:** `GIT_COMMIT`, `GIT_BRANCH` or `BRANCH_NAME`
- **Azure DevOps:** `Build.SourceVersion`, `Build.SourceBranchName`

**For local files (no Pact Broker):**
- No environment variables needed
- Pact files must be committed to repository in `src/test/resources/pacts`

## Key Points

- **Local Pact files:** Place contract files in `src/test/resources/pacts` before verification
- **Provider states:** Read contracts to identify states, then implement handlers
- **State names:** Must match exactly (case-sensitive) between contract and `@State` annotation
- **Provider name:** Must match exactly between `@Provider` annotation and contract provider name
- **Failures:** Fix provider-side issues, inform consumer team for consumer-side issues
- **CI/CD:** Automate verification on every contract publish (if using Pact Broker) or on every commit (if using local files)

## Troubleshooting

**Verification fails with "No state change handler found":**
- Check that `@State` annotation name matches contract state name exactly
- Verify state handler is in the same test class as verification test

**Verification fails with "Provider name mismatch":**
- Check that `@Provider` annotation name matches contract provider name exactly
- Verify provider name in contract file

**Verification fails with "404 Not Found":**
- Check that provider service is running
- Verify endpoint path matches contract request path
- Check that endpoint is implemented in provider service

**Response body mismatch:**
- Check that provider state handler sets up correct test data
- Verify response structure matches contract
- Check for dynamic values (timestamps, IDs) that may need matching rules

## Best Practices

- **Read contracts first:** Identify provider states and interactions before implementing verification
- **Implement states proactively:** Add provider state handlers for all states found in contracts
- **Verify state names:** Ensure state names match exactly (case-sensitive) between contract and code
- **Isolate state setup:** Keep state setup isolated and independent
- **Fix iteratively:** Fix failures one by one, run verification after each fix
- **Coordinate with consumers:** Communicate with consumer team about contract expectations
- **Automate verification:** Set up CI/CD to run verification automatically
