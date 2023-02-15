package io.github.ludorival.pactjvm.mockk.spring.test

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mockk.spring.NonDeterministicPact
import io.github.ludorival.pactjvm.mockk.spring.PREFERRED_SHOPPING_ID
import io.github.ludorival.pactjvm.mockk.spring.USER_ID
import io.github.ludorival.pactjvm.mockk.spring.contracts.willCreateShoppingList
import io.github.ludorival.pactjvm.mockk.spring.contracts.willDeleteShoppingItem
import io.github.ludorival.pactjvm.mockk.spring.contracts.willGetShoppingList
import io.github.ludorival.pactjvm.mockk.spring.contracts.willListTwoShoppingLists
import io.github.ludorival.pactjvm.mockk.spring.contracts.willPatchShoppingItem
import io.github.ludorival.pactjvm.mockk.spring.contracts.willReturnAnErrorWhenCreateShoppingList
import io.github.ludorival.pactjvm.mockk.spring.contracts.willReturnUserProfile
import io.github.ludorival.pactjvm.mockk.spring.contracts.willSetPreferredShoppingList
import io.github.ludorival.pactjvm.mockk.spring.contracts.willUpdateShoppingList
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice.ShoppingServiceClient
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserServiceClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI

@ExtendWith(NonDeterministicPact::class)
open class ShoppingListAppTest {

    @MockK
    protected lateinit var restTemplate: RestTemplate

    @InjectMockKs
    protected lateinit var userServiceClient: UserServiceClient

    @InjectMockKs
    protected lateinit var shoppingServiceClient: ShoppingServiceClient

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `should create a shopping list`() {
        given {
            restTemplate.willCreateShoppingList("create empty shopping list")

        } `when` {
            shoppingServiceClient.createShoppingList(USER_ID, "My Shopping list")
        } then {
            assertEquals(1, it.id)
            assertEquals("My Shopping list", it.title)
            assertEquals(USER_ID, it.userId)
        }
    }

    @Test
    fun `should get the current shopping list and update the item quantity`() {
        given {
            restTemplate.willReturnUserProfile()
            restTemplate.willGetShoppingList()
            restTemplate.willPatchShoppingItem()

        } `when` {
            userServiceClient.getUserProfile(USER_ID)
        } and {
            shoppingServiceClient.getShoppingList(USER_ID, it.userPreferences.preferredShoppingListId)
        } and {
            shoppingServiceClient.patchItem(it, it.items.first { item -> item.name == "Banana" }.copy(quantity = 3))
        } then {
            assertEquals(2, it.id)
            assertEquals("Banana", it.name)
            assertEquals(3, it.quantity)
        }
    }

    @Test
    fun `should get all shopping list and delete the last one`() {
        given {
            restTemplate.willListTwoShoppingLists()
            restTemplate.willDeleteShoppingItem()

        } `when` {
            shoppingServiceClient.getAllShoppingLists(USER_ID)
        } and {
            shoppingServiceClient.deleteShoppingList(it.last())
        } then {
            verify {
                restTemplate.delete(any<URI>())
            }
        }
    }

    @Test
    fun `should update a shopping list`() {
        given {
            restTemplate.willListTwoShoppingLists()
            restTemplate.willUpdateShoppingList()
        } `when` {
            shoppingServiceClient.getAllShoppingLists(USER_ID)
        } and {
            shoppingServiceClient.updateShoppingList(it.last().copy(title = "My updated shopping list"))
        } then {
            verify {
                restTemplate.put(
                    any<URI>(),
                    match<Map<String, String>> { it.get("name") == "My updated shopping list" })
            }
        }
    }

    @Test
    fun `should return an error when create a bad shopping list`() {
        given {
            restTemplate.willReturnAnErrorWhenCreateShoppingList()
        } `when` {
            assertThrows<HttpClientErrorException> {
                shoppingServiceClient.createShoppingList(
                    USER_ID,
                    "Unexpected character \\s"
                )
            }
        } then {
            assertEquals(HttpStatus.BAD_REQUEST, it.statusCode)
        }
    }


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
            assertEquals(USER_ID, it.id)
            assertEquals("User name", it.name)
            assertEquals("user@email.com", it.email)
            assertEquals(PREFERRED_SHOPPING_ID, it.userPreferences.preferredShoppingListId)
        }
    }

}
