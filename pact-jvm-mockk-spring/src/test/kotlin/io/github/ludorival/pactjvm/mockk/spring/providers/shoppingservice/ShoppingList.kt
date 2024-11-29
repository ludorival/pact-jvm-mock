package io.github.ludorival.pactjvm.mockk.spring.providers.shoppingservice

import java.time.LocalDate

data class ShoppingList(
    val id: Long,
    val title: String,
    val userId: Long,
    val items: List<Item> = emptyList(),
    val createdAt: LocalDate = LocalDate.now()
) {

    data class Item(val id: Long = 0, val name: String = "", val quantity: Int = 0)
}
