package io.github.ludorival.pactjvm.mock.test.verifier

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Consumer
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.spring6.PactVerificationSpring6Provider
import io.github.ludorival.pactjvm.mock.test.PREFERRED_SHOPPING_LIST
import io.github.ludorival.pactjvm.mock.test.SHOPPING_LIST_TO_DELETE
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingServiceApplication
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingServiceRestController
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI

import org.springframework.beans.factory.annotation.Autowired

@ActiveProfiles("shopping-service")
@Tag("contract-test")
@SpringBootTest(
    classes = [ShoppingServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Provider("shopping-service")
@Consumer("shopping-webapp")
@PactFolder("pacts")
class ShoppingServicePactVerifier {

    @Autowired private lateinit var restController: ShoppingServiceRestController
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        try {
            context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
            restController.apply {
                clearAll()
                putShoppingList(PREFERRED_SHOPPING_LIST)
                putShoppingList(SHOPPING_LIST_TO_DELETE)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to setup test context: ${e.message}", e)
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpring6Provider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("the shopping list is empty")
    fun setupEmptyShoppingList() {
        restController.clearAll()
    }

    @State("The request should return a 400 Bad request")
    fun setupBadRequestState() {
        // No setup needed as the validation is handled in the controller
    }

}