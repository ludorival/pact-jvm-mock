package io.github.ludorival.pactjvm.mock.test.consumer.userservice

import io.github.ludorival.pactjvm.mock.test.consumer.safeValue
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import io.github.ludorival.pactjvm.mock.test.userservice.UserPreferences
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile  

class UserServiceClient(private val restTemplate: RestTemplate) {

    private val prefixUrl = URI.create("http://localhost:1234")
    fun getUserProfile(userId: Long): UserProfile {
        return restTemplate.getForEntity(
            "$prefixUrl/user-service/v1/user/{userId}",
            UserProfile::class.java,
            userId
        ).body
            ?: error("Expect to have a non null body")
    }

    fun setPreferredShoppingList(userId: Long, shoppingListId: Long): UserProfile {
        return restTemplate.exchange(
            RequestEntity(
                UserPreferences(shoppingListId),
                HttpMethod.PUT,
                UriComponentsBuilder.newInstance().uri(prefixUrl).path("/user-service/v1/user/{userId}").build(userId)
            ),
            UserProfile::class.java
        ).safeValue()
    }

}
