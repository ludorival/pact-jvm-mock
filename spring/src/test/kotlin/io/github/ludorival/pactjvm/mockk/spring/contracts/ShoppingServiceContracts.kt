package io.github.ludorival.pactjvm.mockk.spring.contracts

import io.github.ludorival.pactjvm.mockk.spring.EMPTY_SHOPPING_LIST
import io.github.ludorival.pactjvm.mockk.spring.PREFERRED_SHOPPING_LIST
import io.github.ludorival.pactjvm.mockk.spring.SHOPPING_LIST_TO_DELETE
import io.github.ludorival.pactjvm.mockk.spring.USER_ID
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mockk.willRespond
import io.github.ludorival.pactjvm.mockk.willRespondWith
import io.mockk.every
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI


fun RestTemplate.willCreateShoppingList() = every {
    postForEntity(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList::class.java)
    )
} willRespond ResponseEntity.ok(
    EMPTY_SHOPPING_LIST
)

fun RestTemplate.willGetShoppingList() = every {
    getForEntity(
        match<String> { it.contains("shopping-service/user/$USER_ID/list") },
        ShoppingList::class.java
    )
} willRespond ResponseEntity.ok(
    PREFERRED_SHOPPING_LIST
)

fun RestTemplate.willPatchShoppingItem() = every {
    patchForObject(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList.Item::class.java)
    )
} willRespondWith {
    val item = arg<ShoppingList.Item>(1)
    item
}

fun RestTemplate.willListTwoShoppingLists() = every {
    exchange(
        match<URI> { it.path.contains("shopping-service") },
        HttpMethod.GET,
        any(),
        any<ParameterizedTypeReference<List<ShoppingList>>>()
    )
} willRespond ResponseEntity.ok(
    listOf(
        PREFERRED_SHOPPING_LIST,
        SHOPPING_LIST_TO_DELETE
    )
)

fun RestTemplate.willDeleteShoppingItem() = every {
    delete(
        match<URI> { it.path.contains("shopping-service") },
    )
} willRespondWith {
    println("I can have access to $args")
}

fun RestTemplate.willUpdateShoppingList() = every {
    put(
        match<URI> { it.path.contains("shopping-service") },
        any()
    )
} willRespondWith {
    println("I can have access to scope $scope")
}
