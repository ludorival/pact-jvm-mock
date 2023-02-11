package io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice

import org.springframework.web.client.RestTemplate

class UserServiceClient(private val restTemplate: RestTemplate) {

    fun getUserProfile(userId: Long): UserProfile {
        return restTemplate.getForEntity("/user-service/v1/user/${userId}", UserProfile::class.java).body ?: error("Expect to have a non null body")
    }

    fun setPreferredShoppingList(userId: Long, shoppingListId: Long) {
        return restTemplate.put("/user-service/v1/user/${userId}", UserPreferences(shoppingListId))
    }

}
