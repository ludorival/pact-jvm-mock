package io.github.ludorival.pactjvm.mock.spring

import io.github.ludorival.pactjvm.mock.spring.providers.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mock.spring.providers.userservice.UserPreferences
import io.github.ludorival.pactjvm.mock.spring.providers.userservice.UserProfile
import java.time.LocalDate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

const val USER_ID = 123L
const val PREFERRED_SHOPPING_ID = 1L

val USER_PROFILE = UserProfile(
    USER_ID, "User name", "user@email.com", UserPreferences(
        PREFERRED_SHOPPING_ID
    )
)
val EMPTY_SHOPPING_LIST = ShoppingList(
    id = 1,
    userId = USER_ID,
    title = "My Shopping list",
    items = emptyList(),
    createdAt = LocalDate.parse("2023-01-01")
)

val PREFERRED_SHOPPING_LIST = ShoppingList(
    id = PREFERRED_SHOPPING_ID,
    userId = USER_ID,
    title = "My Favorite Shopping list",
    items = listOf(
        ShoppingList.Item(1L, "Apple", 2),
        ShoppingList.Item(2L, "Banana", 2)
    ),
    createdAt = LocalDate.parse("2023-01-01")

)

val SHOPPING_LIST_TO_DELETE = ShoppingList(
    id = PREFERRED_SHOPPING_ID + 1,
    userId = USER_ID,
    title = "My Shopping list to delete",
    items = listOf(
        ShoppingList.Item(1L, "Chicken", 2),
        ShoppingList.Item(2L, "Beed", 1)
    ),
    createdAt = LocalDate.parse("2023-01-01")
)
