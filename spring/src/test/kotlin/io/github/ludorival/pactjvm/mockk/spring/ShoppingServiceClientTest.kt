package io.github.ludorival.pactjvm.mockk.spring

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice.ShoppingServiceClient
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserProfile
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserServiceClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

class ShoppingServiceClientTest {

    @MockK
    private lateinit var restTemplate: RestTemplate

    @InjectMockKs
    private lateinit var userServiceClient: UserServiceClient

    @InjectMockKs
    private lateinit var shoppingServiceClient: ShoppingServiceClient

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `should create a shopping list`() {
        given {
            every {
                restTemplate.postForEntity(
                    match<URI> { it.path.contains("shopping-service") },
                    any(),
                    eq(ShoppingList::class.java)
                )
            } returns ResponseEntity.ok(
                EMPTY_SHOPPING_LIST
            )
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
            every {
                restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
            } returns ResponseEntity.ok(
                USER_PROFILE
            )
            every {
                restTemplate.getForEntity(
                    match<String> { it.contains("shopping-service/user/$USER_ID/list") },
                    ShoppingList::class.java
                )
            } returns ResponseEntity.ok(
                PREFERRED_SHOPPING_LIST
            )
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
            every {
                restTemplate.exchange(
                    match<URI> { it.path.contains("shopping-service") },
                    HttpMethod.GET,
                    any(),
                    any<ParameterizedTypeReference<List<ShoppingList>>>()
                )
            } returns ResponseEntity.ok(
                listOf(
                    PREFERRED_SHOPPING_LIST,
                    SHOPPING_LIST_TO_DELETE
                )
            )
            every {
                restTemplate.delete(
                    match<URI> { it.path.contains("shopping-service") },
                )
            } answers {
            }
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
            every {
                restTemplate.exchange(
                    match<URI> { it.path.contains("shopping-service") },
                    HttpMethod.GET,
                    any(),
                    any<ParameterizedTypeReference<List<ShoppingList>>>()
                )
            } returns ResponseEntity.ok(
                listOf(
                    PREFERRED_SHOPPING_LIST,
                    SHOPPING_LIST_TO_DELETE
                )
            )
            every {
                restTemplate.put(
                    match<URI> { it.path.contains("shopping-service") },
                    any()
                )
            } answers {
            }
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

}
