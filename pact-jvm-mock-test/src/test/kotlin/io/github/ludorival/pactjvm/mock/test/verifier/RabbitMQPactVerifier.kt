package io.github.ludorival.pactjvm.mock.test.verifier

import au.com.dius.pact.provider.PactVerifyProvider
import au.com.dius.pact.provider.junit5.MessageTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Consumer
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.spring6.PactVerificationSpring6Provider
import io.github.ludorival.pactjvm.mock.test.orderservice.OrderMessage
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@ActiveProfiles("order-service")
@Tag("contract-test")
@SpringBootTest(classes = [io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingServiceApplication::class])
@Provider("shopping-service")
@Consumer("order-service")
@PactFolder("pacts")
open class RabbitMQPactVerifier {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = MessageTestTarget()
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpring6Provider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("shopping list ordered")
    fun setupShoppingListOrderedState(params: Map<String, Any>) {
        // State setup if needed
        // The state data is available in the params map
        println("Setting up state with params: $params")
    }

    @PactVerifyProvider("Shopping list ordered message")
    fun verifyShoppingListOrderedMessage(): String {
        // Create a sample shopping list that matches the expected format
        val shoppingList = ShoppingList(
            id = 123L,
            title = "My Shopping List",
            userId = 456L,
            items = listOf(
                ShoppingList.Item(1L, "Apple", 2),
                ShoppingList.Item(2L, "Banana", 3)
            ),
            createdAt = LocalDate.parse("2024-01-21")
        )

        // Convert the shopping list to an order message
        val orderMessage = OrderMessage(
            shoppingListId = shoppingList.id.toString(),
            userId = shoppingList.userId,
            items = shoppingList.items.map { OrderMessage.OrderItem(it.name, it.quantity) }
        )

        // Serialize the message to JSON
        return objectMapper.writeValueAsString(orderMessage)
    }
} 