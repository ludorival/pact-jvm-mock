package io.github.ludorival.pactjvm.mockk

import io.github.ludorival.kotlintdd.SimpleGivenWhenThen.given
import io.github.ludorival.kotlintdd.then
import io.github.ludorival.kotlintdd.`when`
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.shoppingservice.ShoppingServiceClient
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserPreferences
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserProfile
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserServiceClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

@ExtendWith(PactExtension::class)
class UserServiceClientTest {

    @MockK
    private lateinit var restTemplate: RestTemplate

    @InjectMockKs
    private lateinit var userServiceClient: UserServiceClient

    @InjectMockKs
    private lateinit var shoppingServiceClient: ShoppingServiceClient

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

//    @BeforeEach
//    fun setupPact() {
//        pact.setPactDirectory("src/test/resources/pact")
//        pact.setDefaultObjectMapper(
//            Jackson2ObjectMapperBuilder().serializerByType(
//                LocalDateTime::class.java, serializerAsDefault<LocalDateTime>("2023-01-01-00:00:00")
//            ).serializerByType(
//                UUID::class.java, serializerWith<UUID> {
//                    it.writeString(
//                        when (it.outputContext.currentName) {
//                            "transaction_id" -> "3c2f4340-67a3-4639-bb6f-a146103f3dbd"
//                            else             -> "478218ec-c7f2-4142-b3b2-1aea8e8a7c2d"
//                        }
//                    )
//                }
//            ).build()
//        )
//    }

    @Test
    fun `should get the user profile`() {
        given {
            every {
                restTemplate.getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
            } returns ResponseEntity.ok(
                USER_PROFILE
            )
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
            every {
                restTemplate.postForEntity(
                    match<URI> { it.path.contains("shopping-service") },
                    any(),
                    eq(ShoppingList::class.java)
                )
            } returns ResponseEntity.ok(
                PREFERRED_SHOPPING_LIST
            )

            every {
                restTemplate.put(match<String> { it.contains("user-service") }, any())
            } answers {

            }

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
