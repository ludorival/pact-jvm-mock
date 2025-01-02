package io.github.ludorival.pactjvm.mock.test.userservice

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.time.LocalDate

@RestController
@RequestMapping("/user-service")
open class UserServiceRestController {
    private val logger = org.slf4j.LoggerFactory.getLogger(UserServiceRestController::class.java)

    data class UpdatePreferencesRequest(val preferredShoppingListId: Long = 0L)

    // In-memory storage for demo purposes
    private val users = ConcurrentHashMap<Long, UserProfile>()

    init {
        // Initialize with test data
        users[123L] = UserProfile(
            id = 123,
            name = "User name",
            email = "user@email.com",
            userPreferences = UserPreferences(preferredShoppingListId = 1)
        )
    }

    @GetMapping("/v1/user/{userId}")
    open fun getUser(@PathVariable userId: Long): ResponseEntity<UserProfile> {
        return users[userId]?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/v1/user/{userId}")
    open fun updateUserPreferences(
        @PathVariable userId: Long,
        @RequestBody request: UpdatePreferencesRequest
    ): ResponseEntity<UserProfile> {
        return users[userId]?.let { user ->
            val updatedUser = user.copy(
                userPreferences = UserPreferences(request.preferredShoppingListId)
            )
            users[userId] = updatedUser
            ResponseEntity.ok(updatedUser)
        } ?: ResponseEntity.notFound().build()
    }

    // Test support method
    fun setupTestUser(user: UserProfile) {
        users[user.id] = user
    }
} 