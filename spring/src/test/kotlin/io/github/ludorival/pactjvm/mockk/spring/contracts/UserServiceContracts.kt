package io.github.ludorival.pactjvm.mockk.spring.contracts

import io.github.ludorival.pactjvm.mockk.spring.USER_PROFILE
import io.github.ludorival.pactjvm.mockk.spring.fakeapplication.infra.userservice.UserProfile
import io.github.ludorival.pactjvm.mockk.willRespondWith
import io.mockk.every
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate


fun RestTemplate.willReturnUserProfile() = every {
    getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java)
} willRespondWith {
    description = "get the user profile"
    providerStates = listOf("The user has a preferred shopping list")
    ResponseEntity.ok(
        USER_PROFILE
    )
}

fun RestTemplate.willSetPreferredShoppingList() = every {
    put(match<String> { it.contains("user-service") }, any())
} willRespondWith {

}
