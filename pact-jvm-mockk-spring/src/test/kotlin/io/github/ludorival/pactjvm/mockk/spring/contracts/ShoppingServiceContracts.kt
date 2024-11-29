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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI


fun RestTemplate.willCreateShoppingList(description: String? = null) = every {
    postForEntity(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList::class.java)
    )
} willRespondWith {
    description?.let {
        this.description(it)
    }
    ResponseEntity.ok(
        EMPTY_SHOPPING_LIST
    )
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun RestTemplate.willReturnAnErrorWhenCreateShoppingList() = every {
    postForEntity(
        match<URI> { it.path.contains("shopping-service") },
        any(),
        eq(ShoppingList::class.java)
    )
} willRespondWith {
    description("should return a 400 Bad request")
    providerState("The request should return a 400 Bad request")
    anError(ResponseEntity.badRequest().body("The title contains unexpected character"))
}

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
    description("Patch a shopping item")
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
} willRespondWith {
    description("list two shopping lists")
    println(
        "I can have access to $args - $matcher - " +
            "$self - $nArgs -${firstArg<Any>()} - ${secondArg<Any>()} ${thirdArg<Any>()} ${lastArg<Any>()}"
    )
    ResponseEntity.ok(
        listOf(
            PREFERRED_SHOPPING_LIST,
            SHOPPING_LIST_TO_DELETE
        )
    )
}

fun RestTemplate.willDeleteShoppingItem() = every {
    delete(
        match<URI> { it.path.contains("shopping-service") },
    )
} willRespondWith {
    description("delete shopping item")

}

fun RestTemplate.willUpdateShoppingList() = every {
    put(
        match<URI> { it.path.contains("shopping-service") },
        any()
    )
} willRespondWith {
    description("update shopping list")
    println("I can have access to scope $scope")

}
