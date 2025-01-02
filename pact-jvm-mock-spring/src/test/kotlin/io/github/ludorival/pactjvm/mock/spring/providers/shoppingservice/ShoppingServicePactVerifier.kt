package io.github.ludorival.pactjvm.mock.spring.providers.shoppingservice

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import io.github.ludorival.pactjvm.mock.spring.EMPTY_SHOPPING_LIST
import io.github.ludorival.pactjvm.mock.spring.PREFERRED_SHOPPING_LIST
import io.github.ludorival.pactjvm.mock.spring.SHOPPING_LIST_TO_DELETE
import io.github.ludorival.pactjvm.mock.spring.USER_ID
import org.springframework.beans.factory.annotation.Autowired

@ActiveProfiles("shopping-service")
@Tag("contract-test")
@SpringBootTest(
    classes = [ShoppingServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Provider("shopping-service")
@PactFolder("pacts")
class ShoppingServicePactVerifier {

    @Autowired private lateinit var restController: ShoppingServiceRestController
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
        restController.apply {
            clearAll()
            putShoppingList(PREFERRED_SHOPPING_LIST)
            putShoppingList(SHOPPING_LIST_TO_DELETE)
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("the shopping list is empty")
    fun setupEmptyShoppingList(params: Map<String, Any>) {
        restController.clearAll()
    }

    @State("The request should return a 400 Bad request")
    fun setupBadRequestState() {
        // No setup needed as the validation is handled in the controller
    }

} 