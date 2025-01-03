package io.github.ludorival.pactjvm.mock.test.verifier

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import io.github.ludorival.pactjvm.mock.test.userservice.UserServiceApplication
import io.github.ludorival.pactjvm.mock.test.userservice.UserServiceRestController
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile
import io.github.ludorival.pactjvm.mock.test.userservice.UserPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import org.springframework.beans.factory.annotation.Autowired

@ActiveProfiles("user-service")
@Tag("contract-test")
@SpringBootTest(
    classes = [UserServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Provider("user-service")
@PactFolder("pacts")
class UserServicePactVerifier {

    @Autowired private lateinit var restController: UserServiceRestController
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

    @State("The user has a preferred shopping list")
    fun setupUserWithPreferredShoppingList(params: Map<String, Any>) {
        val userId = (params["userId"].toString()).toLong()
        val user = UserProfile(
            id = userId,
            name = "User name",
            email = "user@email.com",
            userPreferences = UserPreferences(preferredShoppingListId = 1)
        )
        restController.setupTestUser(user)
    }
}