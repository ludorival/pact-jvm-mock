package io.github.ludorival.pactjvm.mock.test.contracts

import io.github.ludorival.pactjvm.mock.test.USER_ID
import io.github.ludorival.pactjvm.mock.test.USER_PROFILE
import io.github.ludorival.pactjvm.mock.test.userservice.UserProfile
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import io.github.ludorival.pactjvm.mock.state
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate


fun RestTemplate.willReturnUserProfile() = uponReceiving {
    getForEntity(match<String> { it.contains("user-service") }, UserProfile::class.java, *anyVararg())
} withDescription "get the user profile" given state("The user has a preferred shopping list", mapOf("userId" to USER_ID)) returns 
    ResponseEntity.ok(
        USER_PROFILE
    )


fun RestTemplate.willSetPreferredShoppingList() = uponReceiving {
    exchange(any(), UserProfile::class.java)
} returns 
    ResponseEntity.ok(
        USER_PROFILE
    )

