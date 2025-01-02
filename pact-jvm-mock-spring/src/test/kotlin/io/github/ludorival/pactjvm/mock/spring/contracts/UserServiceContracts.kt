package io.github.ludorival.pactjvm.mock.spring.contracts

import io.github.ludorival.pactjvm.mock.spring.USER_ID
import io.github.ludorival.pactjvm.mock.spring.USER_PROFILE
import io.github.ludorival.pactjvm.mock.spring.providers.userservice.UserProfile
import io.github.ludorival.pactjvm.mock.mockk.willRespondWith
import io.mockk.every
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate


fun RestTemplate.willReturnUserProfile() = every {
    getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java, *anyVararg())
} willRespondWith {
    description("get the user profile")
    providerState("The user has a preferred shopping list", mapOf("userId" to USER_ID))
    ResponseEntity.ok(
        USER_PROFILE
    )
}

fun RestTemplate.willSetPreferredShoppingList() = every {
    exchange(any(), UserProfile::class.java)
} willRespondWith {
    ResponseEntity.ok(
        USER_PROFILE
    )
}
