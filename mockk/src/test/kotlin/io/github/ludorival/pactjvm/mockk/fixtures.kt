package io.github.ludorival.pactjvm.mockk

import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.shoppingservice.ShoppingList
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserPreferences
import io.github.ludorival.pactjvm.mockk.fakeapplication.infra.userservice.UserProfile

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
    items = emptyList()
)

val PREFERRED_SHOPPING_LIST = ShoppingList(
    id = PREFERRED_SHOPPING_ID,
    userId = USER_ID,
    title = "My Favorite Shopping list",
    items = listOf(
        ShoppingList.Item(1L, "Apple", 2),
        ShoppingList.Item(2L, "Banana", 2)
    )
)

val SHOPPING_LIST_TO_DELETE = ShoppingList(
    id = PREFERRED_SHOPPING_ID + 1,
    userId = USER_ID,
    title = "My Shopping list to delete",
    items = listOf(
        ShoppingList.Item(1L, "Chicken", 2),
        ShoppingList.Item(2L, "Beed", 1)
    )
)
