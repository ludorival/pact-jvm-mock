package io.github.ludorival.pactjvm.mock.test.contracts

import io.github.ludorival.pactjvm.mock.Matcher
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.github.ludorival.pactjvm.mock.state
import io.github.ludorival.pactjvm.mock.test.EMPTY_SHOPPING_LIST
import io.github.ludorival.pactjvm.mock.test.PREFERRED_SHOPPING_LIST
import io.github.ludorival.pactjvm.mock.test.SHOPPING_LIST_TO_DELETE
import io.github.ludorival.pactjvm.mock.test.USER_ID
import io.github.ludorival.pactjvm.mock.test.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mock.anError
import java.net.URI
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

fun RestTemplate.willCreateShoppingList(description: String? = null) =
        uponReceiving {
            postForEntity(
                    match<URI> { it.path.contains("shopping-service") },
                    any(),
                    eq(ShoppingList::class.java)
            )
        }
        .withDescription(description)
        .given(state("the shopping list is empty", mapOf("userId" to USER_ID)))
        .macthingResponse { body("created_at", Matcher(Matcher.MatchEnum.TYPE)) } returns
        ResponseEntity.ok(EMPTY_SHOPPING_LIST)

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun RestTemplate.willReturnAnErrorWhenCreateShoppingList() =
        uponReceiving {
            postForEntity(
                    match<URI> { it.path.contains("shopping-service") },
                    any(),
                    eq(ShoppingList::class.java)
            )
        }
        .withDescription("should return a 400 Bad request")
        .given(state("The request should return a 400 Bad request")) throws anError(
        ResponseEntity.badRequest().body("The title contains unexpected character"))

fun RestTemplate.willGetShoppingList() =
        uponReceiving {
            getForEntity(
                    match<String> { it.contains("shopping-service/user/$USER_ID/list") },
                    ShoppingList::class.java
            )
        } returns
        ResponseEntity.ok(PREFERRED_SHOPPING_LIST)

fun RestTemplate.willPatchShoppingItem() =
        uponReceiving {
            patchForObject(
                    match<URI> { it.path.contains("shopping-service") },
                    any(),
                    eq(ShoppingList.Item::class.java)
            )
        }
        .withDescription("Patch a shopping item") answers {
            arg<ShoppingList.Item>(1)
        }

fun RestTemplate.willListTwoShoppingLists() =
        uponReceiving {
            exchange(
                    match<URI> { it.path.contains("shopping-service") },
                    HttpMethod.GET,
                    any(),
                    any<ParameterizedTypeReference<List<ShoppingList>>>()
            )
        }
        .withDescription("list two shopping lists")
        .matchingRequest {
            header("Authorization", Matcher(Matcher.MatchEnum.REGEX, "Bearer .*"))
        }
        .macthingResponse {
            body("[*].id", Matcher(Matcher.MatchEnum.TYPE))
            body("[*].created_at", Matcher(Matcher.MatchEnum.TYPE))
        } returns
        ResponseEntity.ok(listOf(PREFERRED_SHOPPING_LIST, SHOPPING_LIST_TO_DELETE))

fun RestTemplate.willDeleteShoppingItem() =
        uponReceiving {
            delete(
                    match<URI> { it.path.contains("shopping-service") },
            )
        }
        .withDescription("delete shopping item")
        .macthingResponse {} returns
        Unit

fun RestTemplate.willUpdateShoppingList() =
        uponReceiving {
            put(match<URI> { it.path.contains("shopping-service") }, any())
        }
        .withDescription("update shopping list")
        .macthingResponse {} returns
        Unit
