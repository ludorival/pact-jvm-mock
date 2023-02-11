package io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.shoppingservice

import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.safeValue
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

class ShoppingServiceClient(private val restTemplate: RestTemplate) {

    fun getAllShoppingLists(userId: Long): List<ShoppingList> {
        return restTemplate.exchange(
            UriComponentsBuilder.newInstance().path("/shopping-service/user/{userId}").query("limit=30").build(userId),
            HttpMethod.GET,
            HttpEntity<Any>(HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }),
            object : ParameterizedTypeReference<List<ShoppingList>>() {}).safeValue()
    }

    fun getShoppingList(userId: Long, id: Long): ShoppingList {
        return restTemplate.getForEntity("/shopping-service/user/${userId}/list/${id}", ShoppingList::class.java)
            .safeValue()
    }

    fun createShoppingList(userId: Long, title: String): ShoppingList {
        return restTemplate.postForEntity(
            UriComponentsBuilder.newInstance().path("/shopping-service/user/{userId}").build(userId),
            mapOf("title" to title), ShoppingList::class.java
        ).safeValue()
    }

    fun updateShoppingList(shoppingList: ShoppingList) {
        return restTemplate.put(
            UriComponentsBuilder.newInstance().path("/shopping-service/user/{userId}/list/{id}")
                .build(shoppingList.userId, shoppingList.id),
            mapOf("name" to shoppingList.title)
        )
    }

    fun deleteShoppingList(shoppingList: ShoppingList) {
        return restTemplate.delete(
            UriComponentsBuilder.newInstance().path("/shopping-service/user/{userId}/list/{id}")
                .build(shoppingList.userId, shoppingList.id)
        )
    }

    fun patchItem(shoppingList: ShoppingList, item: ShoppingList.Item): ShoppingList.Item {
        return restTemplate.patchForObject(
            UriComponentsBuilder.newInstance().path("/shopping-service/user/{userId}/list/{id}")
                .build(shoppingList.userId, shoppingList.id),
            item,
            ShoppingList.Item::class.java
        ) ?: error("expect a non null shopping list item")
    }


}
