package io.github.ludorival.pactjvm.mockk.spring.test

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mockk.spring.PREFERRED_SHOPPING_ID
import io.github.ludorival.pactjvm.mockk.spring.ShoppingPactExtension
import io.github.ludorival.pactjvm.mockk.spring.USER_ID
import io.github.ludorival.pactjvm.mockk.spring.contracts.willCreateShoppingList
import io.github.ludorival.pactjvm.mockk.spring.contracts.willReturnUserProfile
import io.github.ludorival.pactjvm.mockk.spring.contracts.willSetPreferredShoppingList
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice.ShoppingServiceClient
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserPreferences
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserServiceClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestTemplate

@ExtendWith(ShoppingPactExtension::class)
class UserServiceClientTest {

    @MockK
    private lateinit var restTemplate: RestTemplate

    @InjectMockKs
    private lateinit var userServiceClient: UserServiceClient

    @InjectMockKs
    private lateinit var shoppingServiceClient: ShoppingServiceClient

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `should get the user profile`() {
        given {
            restTemplate.willReturnUserProfile()
        } `when` {
            userServiceClient.getUserProfile(USER_ID)
        } then {
            assertEquals(USER_ID, it.id)
            assertEquals("User name", it.name)
            assertEquals("user@email.com", it.email)
            assertEquals(PREFERRED_SHOPPING_ID, it.userPreferences.preferredShoppingListId)
        }
    }

    @Test
    fun `should set preferred shopping list`() {
        given {
            restTemplate.willCreateShoppingList()
            restTemplate.willSetPreferredShoppingList()

        } `when` {
            shoppingServiceClient.createShoppingList(USER_ID, "My shopping list")
        } and {
            userServiceClient.setPreferredShoppingList(USER_ID, it.id)
        } then {
            verify {
                restTemplate.put(
                    any<String>(),
                    match<UserPreferences> { it.preferredShoppingListId == PREFERRED_SHOPPING_ID })
            }
        }
    }


}
